package weaver.interfaces.workflow.action;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
import weaverjn.util.WSClientUtils;

import java.util.HashMap;

/**
 * Created by dzyq on 2016/4/20.
 */
public class JHWFKAction extends BaseAction {
    public String execute(RequestInfo paramRequestInfo) {
        RecordSet rs = new RecordSet();
        RecordSet rse = new RecordSet();
        //获取入参requestid和workflowid
        String requestid = paramRequestInfo.getRequestid();
        String workflowid = paramRequestInfo.getWorkflowid();
        String maintable = "";
        //查询主表表名
        String strT = "select b.tablename,b.id from workflow_base a,workflow_bill b where a.formid = b.id and a.id = " + workflowid;
        rs.executeSql(strT);
        while (rs.next()) {
            maintable = Util.null2String(rs.getString("tablename"));
        }
        String messages = "";
        int i = 0;
        //查询明细信息
        rs.executeSql("select * from "+maintable+"_dt1 where isnull(zt,0) !=1 and isnull(jsfs,'')!='' and mainid=(select id from "+maintable+" where requestid="+requestid+")");
        while (rs.next()) {
            i += 1;
            String id = Util.null2String(rs.getString("id"));
            String jsfs = Util.null2String(rs.getString("jsfs"));
            String request_jsfs = "";
            String url = "";
            if ("1".equals(jsfs)) {
                request_jsfs = "1";
//                url = "http://pidev.yytex.net:50300/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_VendorPayOutSch1_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI";
                url = "http://piprd.yytex.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_VendorPayOutSch1_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI";
            } else if ("2".equals(jsfs)) {
                request_jsfs = "2";
//                url = "http://pidev.yytex.net:50300/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_VendorPayOutSch2_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI";
                url = "http://piprd.yytex.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_VendorPayOutSch2_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI";
            } else if ("3".equals(jsfs)) {
                request_jsfs = "3";
//                url = "http://pidev.yytex.net:50300/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_VendorPayOutSch3_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI";
                url = "http://piprd.yytex.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_VendorPayOutSch3_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI";
            } else {
                messages += "第" + i + "行，jsfs错误，为\"" + jsfs + "\"\n";
                continue;
            }
            String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:yyjf.com:OA:FI\">\n" +
                    "   <soapenv:Header/>\n" +
                    "   <soapenv:Body>\n" +
                    "      <urn:MT_MainTableId_Oa>\n" +
                    "         <requestId>" + requestid + "</requestId>\n" +
                    "         <id>" + id + "</id>\n" +
                    "         <jsfs>" + request_jsfs + "</jsfs>\n" +
                    "      </urn:MT_MainTableId_Oa>\n" +
                    "   </soapenv:Body>\n" +
                    "</soapenv:Envelope>";
            HashMap<String, String> httpHeaderParm = new HashMap<String, String>();
            httpHeaderParm.put("instId", "10062");
            httpHeaderParm.put("repairType", "RP");
            String response = WSClientUtils.callWebServiceWithHttpHeaderParm(request,
                    url,
                    httpHeaderParm);
            System.out.println("JHWFKAction---->request:\n" + request + "\nJHWFKAction---->response:\n" + response + "\n");
            HashMap<String, String> result = this.getResult(response);
            String isOK = result.get("isOK");
            String Type = result.get("Type");
            String Message = result.get("Message");
            String AccountId = result.get("AccountId");
            if (isOK.equals("1")) {
                if ("S".equals(Type)) {
                    rse.execute("update " + maintable + "_dt1 set zt=1, jg='" + AccountId + "' where id=" + id);
                } else {
                    rse.execute("update " + maintable + "_dt1 set zt=2, jg='" + Message + "' where id=" + id);
                    messages += "第" + i + "行，Type:" + Type + "， Message:" + Message + "\n";
                }
            } else {
                messages += "第" + i + "行，Response:" + response + "\n";
            }
        }
        if (!messages.isEmpty()) {
            paramRequestInfo.getRequestManager().setMessageid("90031");
            paramRequestInfo.getRequestManager().setMessagecontent("提示信息：\n" + messages);
            return "0";
        }
        return Action.SUCCESS;
    }

    public HashMap<String, String> getResult(String response) {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("isOK", "1");
        try {
            Document dom = DocumentHelper.parseText(response);
            Element root = dom.getRootElement();
            Element MT_Oa_Return = root.element("Body").element("MT_Oa_Return");
            String Type = MT_Oa_Return.elementText("Type");
            String Message = MT_Oa_Return.elementText("Message");
            String AccountId = MT_Oa_Return.elementText("AccountId");
            result.put("Type", Type);
            result.put("Message", Message);
            result.put("AccountId", AccountId);
        } catch (DocumentException e) {
            result.put("isOK", "0");
            e.printStackTrace();
        }
        return result;
    }
}
