<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util,weaver.hrm.User,weaver.hrm.HrmUserVarify" %>
<%@ page import="java.util.*" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%

  User user = HrmUserVarify.getUser(request, response) ;
  if(user == null){
    response.sendRedirect("/notice/noright.jsp") ;
    return ;
  }
  Random random = new Random();
  String iId = ""+user.getUID();	//获取当前登录用户ID
  String iSubId = ""+user.getUserSubCompany1(); //获取当前登录用户公司ID
  String iDepId = ""+user.getUserDepartment();	//获取当前登录用户部门ID
  String iUsername = user.getUsername();	//获取当前登录用户姓名
  String iJobId = user.getJobtitle();	//获取当前登录用户职务ID
  String iLoginid=user.getLoginid();

  out.print("当前登录的用户:iId(登录id):"+iId+",iSubId(分部id):"+iSubId+",iDepId(部门id):"+iDepId+",iUsername(姓名):"
  +iUsername+",iJobId(岗位id):"+iJobId+",iLoginid(登录账号):"+iLoginid);

  String url= Util.null2String(request.getParameter("url"));
  String debug= Util.null2String(request.getParameter("debug"));

  url=url.replace("$iId$",iId);
  url=url.replace("$iSubId$",iSubId);
  url=url.replace("$iDepId$",iDepId);
  url=url.replace("$iUsername$",iUsername);
  url=url.replace("$iJobId$",iJobId);
  url=url.replace("$iLoginid$",iLoginid);

  if(!"".equals(url)&&!debug.equals("0")){
    response.sendRedirect(""+url) ;
    return ;
  }

  out.print("<br/><br/><br/>你配置的URL期望为："+url);

%>

<html>
  <body>
  </br>
  </br>
链接中使用<span style="color:blue">$iId$</span>获取当前登录人id；</br>
链接中使用<span style="color:blue">$iSubId$</span>获取当前登录人的分部id；</br>
链接中使用<span style="color:blue">$iDepId$</span>获取当前登录人的部门id；</br>
链接中使用<span style="color:blue">$iUsername$</span>获取当前登录人的姓名；</br>
链接中使用<span style="color:blue">$iJobId$</span>获取当前登录人的岗位；</br>
链接中使用<span style="color:blue">$iLoginid$</span>获取当前登录人的登录账号；</br>

</br>
</br>

原来：http://www.ic1ng.cn?userid=<span style="color:blue">$iId$</span><span style="color:red">&</span>iSubId=<span style="color:blue">$iSubId$</span></br>
改为：http://www.ic1ng.cn?userid=<span style="color:blue">$iId$</span><span style="color:red">%26</span>iSubId=<span style="color:blue">$iSubId$</span></br>
泛微系统后台配置的菜单为:/customization/power.jsp?url=http://www.ic1ng.cn?userid=<span style="color:blue">$iId$</span><span style="color:red">%26</span>iSubId=<span style="color:blue">$iSubId$</span></br>
<span style="color:red">一定要记得转换：& 要改成 %26</span></br>

如果需要看配置的链接是否正确可以添加参数 <span style="color:red">debug=0</span></br>

那么泛微系统后台配置的菜单为:/customization/power.jsp?<span style="color:red">debug=0&</span>url=http://www.ic1ng.cn?userid=<span style="color:blue">$iId$</span><span style="color:red">%26</span>iSubId=<span style="color:blue">$iSubId$</span></br>
</body>
</html>
