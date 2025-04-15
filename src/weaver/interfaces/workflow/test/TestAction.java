//package weaver.interfaces.workflow.test;
//
//import com.weaver.general.Util;
//import cryptix.util.test.BaseTest;
//import org.junit.Test;
//import weaver.conn.RecordSet;
//import weaverjn.util.WSClientUtils;
//
//import java.util.HashMap;
//
//
//public class TestAction extends BaseTest {
//    @Override
//    protected void engineTest() throws Exception {
//
//    }
//
//    @Test
//    public void TestKHSKAction(){
//
//    }
//
//    /**
//     * 获取工作流主表名
//     */
//    protected String getMainTable(RecordSet rs, String workflowid) throws Exception {
//        String maintable = "";
//        String strT = "select b.tablename,b.id from workflow_base a,workflow_bill b where a.formid = b.id and a.id = " + workflowid;
//        rs.executeSql(strT);
//        while (rs.next()) {
//            maintable = Util.null2String(rs.getString("tablename"));
//        }
//        return maintable;
//    }
//
//    /**
//     * 构建SOAP请求
//     */
//    protected String buildSoapRequest(String requestid, String id) {
//        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:yyjf.com:OA:FI\">" +
//                "<soapenv:Header/><soapenv:Body>" +
//                "<urn:MT_MainTableId_Oa><requestId>" + requestid + "</requestId><id>" + id + "</id><jsfs></jsfs></urn:MT_MainTableId_Oa>" +
//                "</soapenv:Body></soapenv:Envelope>";
//    }
//
//    /**
//     * 构建HTTP请求头
//     */
//    protected HashMap<String, String> buildHttpHeaders() {
//        HashMap<String, String> httpHeaderParm = new HashMap<String, String>();
//        httpHeaderParm.put("instId", "10062");
//        httpHeaderParm.put("repairType", "RP");
//        return httpHeaderParm;
//    }
//
//    /**
//     * 调用Web服务
//     */
//    protected String callWebService(String request, HashMap<String, String> httpHeaderParm) {
//        String response = WSClientUtils.callWebServiceWithHttpHeaderParm(request,
//                "http://piprd.xyjk.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_CustomerInComing_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI",
//                httpHeaderParm);
//
//        // 打印请求和响应信息
//        System.out.println("request----:\n" + request + "\nrequest====");
//        System.out.println("response----:\n" + response + "\nresponse====");
//
//        return response;
//    }
//
//}
