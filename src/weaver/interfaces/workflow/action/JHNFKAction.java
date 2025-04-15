package weaver.interfaces.workflow.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
import weaverjn.util.WSClientUtils;

import java.util.HashMap;
import java.util.Iterator;

public class JHNFKAction implements Action {
	private Log log;

	public JHNFKAction() {
		this.log = LogFactory.getLog(JHNFKAction.class.getName());
	}

	public Log getLog() {
		return this.log;
	}

	public void setLog(Log paramLog) {
		this.log = paramLog;
	}

	public String execute(RequestInfo paramRequestInfo) {
		System.out.println("do JHNFKAction on request:start!");
		this.log.info("do JHNFKAction on request:start!");
		try{
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
			String reStr = "";
			int i = 0;
			//查询明细信息
			rs.executeSql("select * from "+maintable+"_dt1 where isnull(zt,0) !=1 and isnull(jsfss,'')!='' and mainid=(select id from "+maintable+" where requestid="+requestid+")");
			while(rs.next()){
				 i++;
				 String jsfss = Util.null2String(rs.getString("jsfss"));
				 String id = Util.null2String(rs.getString("id"));
				 String mainid = Util.null2String(rs.getString("mainid"));
				 if("1".equals(jsfss)){
					 //long t1=System.currentTimeMillis();
					 String request =
							 "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:yyjf.com:OA:FI\">" +
				             "<soapenv:Header/><soapenv:Body>" +
				             "<urn:MT_VendorPayInAcc_Oa><mainid></mainid><id>"+id+"</id><jsfss>X</jsfss></urn:MT_VendorPayInAcc_Oa>" +
				             "</soapenv:Body></soapenv:Envelope>";
					 HashMap<String, String> httpHeaderParm = new HashMap<String, String>();
					 httpHeaderParm.put("instId", "10062");
					 httpHeaderParm.put("repairType", "RP");
					 String s = WSClientUtils.callWebServiceWithHttpHeaderParm(request,
							 "http://piprd.xyjk.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_VendorPayInAccX_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI",
							 httpHeaderParm);
					 System.out.println("return:"+s);
					 this.log.info("return:"+s);
					 Document dom=DocumentHelper.parseText(s);
					 Element root=dom.getRootElement();
					 Iterator iterss = root.elementIterator("Body");
					 while (iterss.hasNext()) {
			             Element recordEless = (Element) iterss.next();
			             Iterator itersElIterator = recordEless.elementIterator("MT_Oa_Return");
			             while (itersElIterator.hasNext()) {
			            	 Element itemEle = (Element) itersElIterator.next();
			            	 String type = itemEle.elementTextTrim("Type");
			            	 String accountid = itemEle.elementTextTrim("AccountId");
			            	 String message = itemEle.elementTextTrim("Message");
			            	 System.out.println("Type:" + type);
			            	 if("S".equals(type)){
								 rse.execute("update "+maintable+"_dt1 set zt=1 , jg='"+accountid+"' where id="+id);
							 }else{
								 rse.execute("update "+maintable+"_dt1 set zt=2 , jg='"+message+"' where id="+id);
								 System.out.println("update "+maintable+"_dt1 set zt=2 and jg='"+message+"' where id="+id);
								 reStr = reStr + " 第"+i+"行:" + message;
							 }
			             }
					 }
//				     long t2=System.currentTimeMillis();  
//				     System.out.println(t2-t1);
//				     this.log.info("time:"+(t2-t1));
				 }
				 if("2".equals(jsfss)){
					 String request =
							 "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:yyjf.com:OA:FI\">" +
				             "<soapenv:Header/><soapenv:Body>" +
				             "<urn:MT_VendorPayInAcc_Oa><mainid></mainid><id>"+id+"</id><jsfss>Y</jsfss></urn:MT_VendorPayInAcc_Oa>" +
				             "</soapenv:Body></soapenv:Envelope>";
					 HashMap<String, String> httpHeaderParm = new HashMap<String, String>();
					 httpHeaderParm.put("instId", "10062");
					 httpHeaderParm.put("repairType", "RP");
					 String s = WSClientUtils.callWebServiceWithHttpHeaderParm(request,
							 "http://piprd.xyjk.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_VendorPayInAccY_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI",
							 httpHeaderParm);
					 System.out.println("return:"+s);
					 Document dom=DocumentHelper.parseText(s);
					 Element root=dom.getRootElement();
					 Iterator iterss = root.elementIterator("Body");
					 while (iterss.hasNext()) {
			             Element recordEless = (Element) iterss.next();
			             Iterator itersElIterator = recordEless.elementIterator("MT_Oa_Return");
			             while (itersElIterator.hasNext()) {
			            	 Element itemEle = (Element) itersElIterator.next();
			            	 String type = itemEle.elementTextTrim("Type");
			            	 String accountid = itemEle.elementTextTrim("AccountId");
			            	 String message = itemEle.elementTextTrim("Message");
			            	 System.out.println("Type:" + type);
			            	 if("S".equals(type)){
								 rse.execute("update "+maintable+"_dt1 set zt=1 , jg='"+accountid+"' where id="+id);
							 }else{
								 rse.execute("update "+maintable+"_dt1 set zt=2 , jg='"+message+"' where id="+id);
								 reStr = reStr + " 第"+i+"行:" + message;
							 }
			             }
					 }
				 }
				 if("3".equals(jsfss)){
					 String request =
							 "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:yyjf.com:OA:FI\">" +
				             "<soapenv:Header/><soapenv:Body>" +
				             "<urn:MT_VendorPayInAcc_Oa><mainid></mainid><id>"+id+"</id><jsfss>Z</jsfss></urn:MT_VendorPayInAcc_Oa>" +
				             "</soapenv:Body></soapenv:Envelope>";
					 HashMap<String, String> httpHeaderParm = new HashMap<String, String>();
					 httpHeaderParm.put("instId", "10062");
					 httpHeaderParm.put("repairType", "RP");
					 String s = WSClientUtils.callWebServiceWithHttpHeaderParm(request,
							 "http://piprd.xyjk.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_VendorPayInAccZ_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI",
							 httpHeaderParm);
					 System.out.println("return:"+s);
					 Document dom=DocumentHelper.parseText(s);
					 Element root=dom.getRootElement();
					 Iterator iterss = root.elementIterator("Body");
					 while (iterss.hasNext()) {
			             Element recordEless = (Element) iterss.next();
			             Iterator itersElIterator = recordEless.elementIterator("MT_Oa_Return");
			             while (itersElIterator.hasNext()) {
			            	 Element itemEle = (Element) itersElIterator.next();
			            	 String type = itemEle.elementTextTrim("Type");
			            	 String accountid = itemEle.elementTextTrim("AccountId");
			            	 String message = itemEle.elementTextTrim("Message");
			            	 System.out.println("Type:" + type);
			            	 if("S".equals(type)){
								 rse.execute("update "+maintable+"_dt1 set zt=1 , jg='"+accountid+"' where id="+id);
							 }else{
								 rse.execute("update "+maintable+"_dt1 set zt=2 , jg='"+message+"' where id="+id);
								 reStr = reStr + " 第"+i+"行:" + message;
							 }
			             }
					 }
				 }
			}
			if(!"".equals(reStr)){
				paramRequestInfo.getRequestManager().setMessageid("90031");
				paramRequestInfo.getRequestManager().setMessagecontent(reStr);
				return "0";
			}
		}catch (Exception e) {
			this.log.info(e.getMessage());
			paramRequestInfo.getRequestManager().setMessageid("90031");
			paramRequestInfo.getRequestManager().setMessagecontent(e.getMessage());
			return "0";
		}
		return Action.SUCCESS;
	}



	/*public static void main(String[] args) {
//		YuYueAction aa = new YuYueAction();
//		RequestInfo request = new RequestInfo();
//		request.setRequestid("108743");
//		request.setWorkflowid("350");
//		aa.execute(request);
		TestInit tt = new TestInit();
		tt.init();
		RecordSet rs = new RecordSet();
	}*/
}