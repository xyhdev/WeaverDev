package weaver.interfaces.workflow.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;
import weaverjn.util.WSClientUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;

public class XYKHSKAction implements Action {
	private Log log;

	public XYKHSKAction() {
		this.log = LogFactory.getLog(XYKHSKAction.class.getName());
	}

	public Log getLog() {
		return this.log;
	}

	public void setLog(Log paramLog) {
		this.log = paramLog;
	}

	public String execute(RequestInfo paramRequestInfo) {
		// 添加基本认证信息
		String username = "wangly";
		String password = "wly926458983";
		String auth = username + ":" + password;
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

		this.log.info("do KHSKAction on request:start!");
		try{
			RecordSet rs = new RecordSet();
			RecordSet rse = new RecordSet();
			//获取入参requestid和workflowid
			String requestid = paramRequestInfo.getRequestid();
			String workflowid = paramRequestInfo.getWorkflowid();
			RequestManager rm = paramRequestInfo.getRequestManager();
			String status = rm.getSrc();
			if(status.equals("submit")) {
				String maintable = "";
				//查询主表表名
				String strT = "select b.tablename,b.id from workflow_base a,workflow_bill b where a.formid = b.id and a.id = " + workflowid;
				rs.executeSql(strT);
				while (rs.next()) {
					maintable = Util.null2String(rs.getString("tablename"));
				}

				//查询明细信息
				rs.executeSql("select * from " + maintable + " where isnull(zt,0) !=1 and requestid=" + requestid);
				if (rs.next()) {
					String id = Util.null2String(rs.getString("id"));
					String request =
							"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:yyjf.com:OA:FI\">" +
									"<soapenv:Header/><soapenv:Body>" +
									"<urn:MT_MainTableId_Oa><requestId>" + requestid + "</requestId><id>" + id + "</id><jsfs></jsfs></urn:MT_MainTableId_Oa>" +
									"</soapenv:Body></soapenv:Envelope>";
					HashMap<String, String> httpHeaderParm = new HashMap<String, String>();
					httpHeaderParm.put("instId", "10062");
					httpHeaderParm.put("repairType", "RP");
					httpHeaderParm.put("Authorization", "Basic " + encodedAuth);
					String s = WSClientUtils.callWebServiceWithHttpHeaderParm(request,
							"http://piprd.xyjk.net:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_OA&receiverParty=&receiverService=&interface=SI_CustomerInComing_Oa_Out&interfaceNamespace=urn:yyjf.com:OA:FI",
							httpHeaderParm);
					System.out.println("request----:\n" + request + "\nrequest====");
					System.out.println("response----:\n" + s + "\nresponse====");
					Document dom = DocumentHelper.parseText(s);
					Element root = dom.getRootElement();
					Iterator iterss = root.elementIterator("Body");
					while (iterss.hasNext()) {
						Element recordEless = (Element) iterss.next();
						Iterator itersElIterator = recordEless.elementIterator("MT_Oa_Return");
						while (itersElIterator.hasNext()) {
							Element itemEle = (Element) itersElIterator.next();
							String type = itemEle.elementTextTrim("Type");
							String accountid = itemEle.elementTextTrim("AccountId");
							String message = itemEle.elementTextTrim("Message");
							if ("S".equals(type)) {
								rse.execute("update " + maintable + " set zt=1 , jg='" + accountid + "' where id=" + id);
							} else {
								rse.execute("update " + maintable + " set zt=2 , jg='" + message + "' where id=" + id);
								paramRequestInfo.getRequestManager().setMessageid("90031");
								paramRequestInfo.getRequestManager().setMessagecontent(message);
								return "0";
							}
						}
					}
				}
			}
		}catch (Exception e) {
			this.log.info(e.getMessage());
			paramRequestInfo.getRequestManager().setMessageid("90031");
			paramRequestInfo.getRequestManager().setMessagecontent(e.getMessage());
			return "0";
		}
		return Action.SUCCESS;
	}

}