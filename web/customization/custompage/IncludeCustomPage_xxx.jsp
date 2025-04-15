<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="java.lang.*" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%
String custompageURI=request.getRequestURI();
int formid = Util.getIntValue(request.getParameter("formid"));          //表单id
%>

<script type="text/javascript" src="/cloudstore/resource/pc/jquery/jquery-1.8.3.min.js?v=2018032011"></script>
<script type="text/javascript" src="/angel/custompage/fna/getFieldInfo.jsp?formid=<%=formid%>"></script>
<script type="text/javascript">
/*主表字段对应的数据库即为该字段的fielid
 var sqr=WfForm.convertFieldNameToId('sqr'); //主表-公司代码（俄）[browser.45481]
 var dt1_xsbscmc=WfForm.convertFieldNameToId('xsbscmc','detail_1'); //明细表1-车船票[number(38,2)]
 if(window.console) {console.log("lth:399=sqr===",sqr);} //2020/9/18 下午4:09 lth    end
 if(window.console) {console.log("lth:399=dt1_xsbscmc===",dt1_xsbscmc);} //2020/9/18 下午4:09 lth    end
*/
//2020/9/18 下午2:47 lth    start
jQuery(document).ready(function(){
//可以在这里做一些有意识的事情
});
//2020/9/18 下午2:47 lth    end
 if(window.console){console.log("lth:338===sqrq_v=",sqrq_v);//sqrq_v对应为主表字段sqrq的值} //2020/9/21 上午10:32 lth    end
 if(window.console) {console.log("lth:841====load <%=custompageURI%> ok! 源码获取：https://gitee.com/L1uTaihong/WeaverEc9CustomDev  表单API访问:https://e-cloudstore.com/doc.html ");} //2020/9/17 下午2:34 lth    用于判断js是否加载完成及引入的js文件路径  end
</script>