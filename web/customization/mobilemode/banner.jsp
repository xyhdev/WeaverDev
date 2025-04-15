<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="net.sf.json.*" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.general.BaseBean" %>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="weaver.hrm.User" %>
<%@ page import="weaver.workflow.mode.FieldInfo" %>
<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="weaver.general.SplitPageUtil"%>
<%@ page import="weaver.general.SplitPageParaBean"%>
<%@ page import="java.util.regex.Matcher"%>
<%@ page import="java.util.regex.Pattern"%>
<%!
public static String getStringNoBlank(String str) {
        if(str!=null && !"".equals(str)) {
            Pattern p = Pattern.compile("\t|\r|\n");
            Matcher m = p.matcher(str);
            String strNoBlank = m.replaceAll("");
            return strNoBlank;
        }else {
            return str;
        }
  }
%>
<%


JSONArray jsonArray = new JSONArray();
User user = HrmUserVarify.getUser (request , response) ;
if (user==null){
   out.print(jsonArray.toString());
    return;
}

JSONObject img1 = new JSONObject();
img1.put("action","javaScript:alert('我是点击事件')");
img1.put("pic_path","/mobilemode/piclibrary/03-E9_flat/slider05.png");
img1.put("pic_desc","专业化");
jsonArray.add(img1);

img1.put("action","https://www.weaver.com.cn/");
img1.put("pic_path","/mobilemode/piclibrary/03-E9_flat/slider03.png");
img1.put("pic_desc","点击跳转页面");
jsonArray.add(img1);



out.clearBuffer();
out.print(jsonArray.toString());
%>
