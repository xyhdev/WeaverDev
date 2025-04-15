package weaver.interfaces.workflow.action;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import weaver.conn.RecordSet;
import weaver.soa.workflow.request.RequestInfo;
import weaverjn.util.WSClientUtils;

import java.util.HashMap;

/**
 * Created by dzyq on 2016/6/30.
 */
public class SBZYWLLYAction extends BaseAction {
    public String execute(RequestInfo requestInfo) {
        System.out.println("---->SBZYWLLYAction start");
        String request_id = requestInfo.getRequestid();
        String workflow_id = requestInfo.getWorkflowid();
        RecordSet rs = new RecordSet();
        String err = "";
        String sql = "select b.tablename,b.id from workflow_base a,workflow_bill b where a.formid = b.id and a.id = " + workflow_id;
        rs.executeSql(sql);
        if (rs.next()) {
            String mainTable = rs.getString("tablename");
            sql = "select id, sqr, sqsj, cbzx from " + mainTable + " where requestid=" + request_id;
            rs.executeSql(sql);
            if (rs.next()) {
                String id = rs.getString("id");
                String sqr = rs.getString("sqr");
                String sqsj = rs.getString("sqsj");
                String cbzx = rs.getString("cbzx");
                String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:yyjf.com:OA:MM\">\n" +
                        "   <soapenv:Header/>\n" +
                        "   <soapenv:Body>\n" +
                        "      <urn:MT_DevicePickList_Oa>\n" +
                        "         <row>\n" +
                        "            <id>" + id + "</id>\n" +
                        "            <sqr>" + sqr + "</sqr>\n" +
                        "            <sqsj>" + sqsj + "</sqsj>\n" +
                        "            <cbzx>" + cbzx + "</cbzx>\n" +
                        "         </row>\n" +
                        "      </urn:MT_DevicePickList_Oa>\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>";
                HashMap<String, String> httpHeaderParm = new HashMap<String, String>();
                httpHeaderParm.put("instId", "10062");
                httpHeaderParm.put("repairType", "RP");
                String response = WSClientUtils.callWebServiceWithHttpHeaderParm(request,
                        "http://piprd.yytex.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_DevicePickList_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:MM",
                        httpHeaderParm);
                System.out.println("---->request:\n" + request + "\n---->response:\n" + response);
                HashMap<String, String> result = getResult(response);
                String ok = result.get("OK");
                String message = result.get("message");
                String PickListID = result.get("PickListID");
                if (ok.equals("1")) {
                    System.out.println("---->RequestId:" + request_id);
                    System.out.println("---->message:" + message);
                    System.out.println("---->PickListID:" + PickListID);
                    if (message.startsWith("类型：S----凭证") && message.endsWith("已记帐")) {
                        RecordSet rs2 = new RecordSet();
                        rs2.executeSql("update " + mainTable + " set zt=1, jg='" + PickListID + "' where id=" + id);
                    } else {
                        RecordSet rs2 = new RecordSet();
                        rs2.executeSql("update " + mainTable + " set zt=2, jg='" + message + "' where id=" + id);
                        err = message;
                    }
                } else {
                    err = "Response 格式错误？" + response;
                }
            } else {
                err = "error code: 1";
            }
        } else {
            err = "error code: 2";
        }
        if (!err.isEmpty()) {
            requestInfo.getRequestManager().setMessageid("90031");
            requestInfo.getRequestManager().setMessagecontent(err);
            return "0";
        }
        return SUCCESS;
    }
    public HashMap<String, String> getResult(String response) {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("OK", "1");
        try {
            Document dom = DocumentHelper.parseText(response);
            Element root = dom.getRootElement();
            Element MT_Oa_Return = root.element("Body").element("MT_OA_RETURN");
            result.put("message", MT_Oa_Return.elementText("message"));
            result.put("PickListID", MT_Oa_Return.elementText("PickListID"));
        } catch (DocumentException e) {
            result.put("OK", "0");
            e.printStackTrace();
        }
        return result;
    }
}
