<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util"%>
<%@page import="weaver.hrm.User"%>
<%@page import="weaver.hrm.HrmUserVarify"%>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
/*
此页面需要配合【流程引擎】【路径设置】【***流程】【基础设置】【提醒设置】的功能使用
适用于流程提交以后需要打开某个指定页面的操作，并支持传递流程ID和请求ID两个参数
*/
//检查是否有权限
User user = HrmUserVarify.checkUser(request,response);
if(user == null){
    	return;
}


String requestid = Util.null2String(request.getParameter("requestid"));
String workflowid = Util.null2String(request.getParameter("workflowid"));

String nodeid = "";
String status = "";
rs.execute("select currentnodeid,status,requestmark from workflow_requestbase where requestid='"+requestid+"'");
if(rs.next()){
 nodeid = rs.getString("currentnodeid");
 status = rs.getString("status");
}

String iscolsefalg = "0";
String wfnodeid = ","+workflowid+"$"+nodeid+",";
String workflownodeid = rs.getPropValue("Angel_FormBillID","workflownodeid");
if((","+workflownodeid+",").indexOf(wfnodeid) > -1) {
 iscolsefalg = "1";
}
//如果在需要打开的节点，则跳转到对应的流程表单的界面
if ("1".equals(iscolsefalg)) {
  response.sendRedirect("/spa/workflow/static4form/index.html?#/main/workflow/req?requestid="+requestid);
  return;
}
%>

<script>

function closeWebPage() {
    if (navigator.userAgent.indexOf("MSIE") > 0) { // IE
        if (navigator.userAgent.indexOf("MSIE 6.0") > 0) {// IE6
            window.opener = null;
            window.close();
        } else {// IE6+
            window.open('', '_top');
            window.top.close();
        }
    } else if (navigator.userAgent.indexOf("Firefox") > 0 || navigator.userAgent.indexOf("Presto") > 0) {// FF和Opera
        window.location.href = 'about:blank';
        window.close();// 火狐默认状态非window.open的页面window.close是无效的
    } else {
        window.opener = null;
        window.open('', '_self', '');
        window.close();
    }
}
if("<%=iscolsefalg%>"=="0"){//不在指定范围之类的流程直接关闭表单
 closeWebPage();
}
</script>
