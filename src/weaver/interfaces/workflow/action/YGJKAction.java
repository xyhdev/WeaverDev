package weaver.interfaces.workflow.action;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
import weaverjn.util.WSClientUtils;
//import weaverjn.yuyue.SI_EmployDebt_Oa_OutServiceStub;

import java.util.HashMap;

/**
 * Created by dzyq on 2016/4/8.
 */
public class YGJKAction extends BaseAction {
    public String execute(RequestInfo paramRequestInfo) {
        getLog().info("do YGJKAction on request:start!");
        System.out.println("do YGJKAction on request:start!");
        try{
            RecordSet rs = new RecordSet();
            RecordSet rse = new RecordSet();
            //获取入参requestid和workflowid
            String requestid = paramRequestInfo.getRequestid();
            String workflowid = paramRequestInfo.getWorkflowid();
            String maintable = "";
            //查询主表表名
            String SQL = "select b.tablename,b.id from workflow_base a,workflow_bill b where a.formid = b.id and a.id = " + workflowid;
            rs.executeSql(SQL);
            while (rs.next()) {
                maintable = Util.null2String(rs.getString("tablename"));
            }
            //查询主表信息
            rs.executeSql("select * from "+maintable+" where isnull(zt,0) !=1 and requestid="+requestid);
            if(rs.next()){
                String id = Util.null2String(rs.getString("id"));
                String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:yyjf.com:OA:FI\">\n" +
                        "   <soapenv:Header/>\n" +
                        "   <soapenv:Body>\n" +
                        "      <urn:MT_MainTableId_Oa>\n" +
                        "         <requestId>" + requestid + "</requestId>\n" +
                        "         <id>" + id + "</id>\n" +
                        "      </urn:MT_MainTableId_Oa>\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>";
                HashMap<String, String> httpHeaderParm = new HashMap<String, String>();
                httpHeaderParm.put("instId", "10062");
                httpHeaderParm.put("repairType", "RP");
                String response = WSClientUtils.callWebServiceWithHttpHeaderParm(request,
//                        "http://pidev.yytex.net:50300/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_EmployDebt_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI",
                        "http://piprd.yytex.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_EmployDebt_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI",
                        httpHeaderParm);
                HashMap<String, String> result = this.getResult(response);
                String isOK = result.get("isOK");
                String Type = result.get("Type");
                String Message = result.get("Message");
                String AccountId = result.get("AccountId");
                if (isOK.equals("1")) {
                    if ("S".equals(Type)) {
                        rse.execute("update " + maintable + " set zt=1, jg='" + AccountId + "' where id=" + id);
                    } else {
                        rse.execute("update " + maintable + " set zt=2, jg='" + Message + "' where id=" + id);
                        paramRequestInfo.getRequestManager().setMessageid("90031");
                        paramRequestInfo.getRequestManager().setMessagecontent(Message);
                        return "0";
                    }
                } else {
                    paramRequestInfo.getRequestManager().setMessageid("90031");
                    paramRequestInfo.getRequestManager().setMessagecontent("Response 格式不正确！" + response);
                    return "0";
                }
                /*SI_EmployDebt_Oa_OutServiceStub ss = new SI_EmployDebt_Oa_OutServiceStub();
                SI_EmployDebt_Oa_OutServiceStub.MT_MainTableId_Oa mT_MainTableId_Oa = new SI_EmployDebt_Oa_OutServiceStub.MT_MainTableId_Oa();
                SI_EmployDebt_Oa_OutServiceStub.DT_MainTableId_Oa param = new SI_EmployDebt_Oa_OutServiceStub.DT_MainTableId_Oa();
                param.setId(id);
                param.setRequestId(requestid);
                mT_MainTableId_Oa.setMT_MainTableId_Oa(param);
                SI_EmployDebt_Oa_OutServiceStub.MT_Oa_Return re = ss.sI_EmployDebt_Oa_Out(mT_MainTableId_Oa);
                SI_EmployDebt_Oa_OutServiceStub.DT_Oa_Return ret = re.getMT_Oa_Return();
                String accountid = ret.getAccountId();
                String type = ret.getType();
                String message = ret.getMessage();
                if("S".equals(type)){
                    rse.execute("update "+maintable+" set zt=1 and jg='"+accountid+"' where id="+id);
                }else{
                    rse.execute("update "+maintable+" set zt=2 and jg='"+message+"' where id="+id);
                    paramRequestInfo.getRequestManager().setMessageid("90031");
                    paramRequestInfo.getRequestManager().setMessagecontent(message);
                }*/
            }
        }catch (Exception e) {
            getLog().info(e.getMessage());
            paramRequestInfo.getRequestManager().setMessageid("90031");
            paramRequestInfo.getRequestManager().setMessagecontent(e.getMessage());
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
