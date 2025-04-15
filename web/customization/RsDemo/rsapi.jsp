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
            String strNoBlank = m.replaceAll(" ");
            p = Pattern.compile("\\&[a-zA-Z]{1,10};");
            m = p.matcher(strNoBlank);
            strNoBlank = m.replaceAll(" ");
            strNoBlank= strNoBlank.replace("  "," ");
            return strNoBlank;
        }else {
            return str;
        }
  }
%>
<%
JSONObject resultObj = new JSONObject();
resultObj.put("v", "2020071601");
resultObj.put("status", 0);

User user = HrmUserVarify.getUser (request , response) ;
if (user==null){
    resultObj.put("info", "登录信息失效");
    return;
}
int userid=user.getUID();
int seclevel= Util.getIntValue(user.getSeclevel(),0);
 RecordSet rs2 = new RecordSet();
 RecordSet rs1 = new RecordSet();
 String apikey =  Util.null2String(request.getParameter("apikey"));
 resultObj.put("apikey", apikey);
 String method =  Util.null2String(request.getParameter("method"));
 String debug =  Util.null2String(request.getParameter("debug"));
 boolean operatelevel=false;
 boolean operaterole=false;
 rs1.execute("select * from uf_sqlset where apikey='"+apikey+"'");
 if(rs1.first()){
   String  roleid=  Util.null2String(rs1.getString("fwqxjs"));//角色ID;
   if("".equals(roleid))roleid="-1";
   int  seclevelstart=  Util.getIntValue(Util.null2String(rs1.getString("fwqxaqjb")),-1);//安全级别开始;
   int  seclevelend=  Util.getIntValue(Util.null2String(rs1.getString("fwqxaqjbjs")),-1);//安全级别结束;


    //登录人的安全级别在设置的范围内 或者在设置的角色内
    if((seclevelstart==-1&seclevelend==-1)||(seclevelstart<=seclevel&&seclevel<=seclevelend)){//安全级别不受限
      operatelevel=true;
    }

    rs2.execute("select id from hrmrolemembers where roleid in （"+roleid+"） and resourceid ='"+user.getUID()+"'");
    if("-1".equals(roleid)||rs2.next()){//角色有权限
      operaterole=true;
    }
    String  sql= rs1.getString("sqlfrom");
    if(debug.equals("1")){
      resultObj.put("原始sql", sql);
      resultObj.put("可查看角色", roleid+":"+operaterole);
      resultObj.put("可查看安全级别", ""+seclevelstart+" - "+seclevelend+":"+operatelevel);
    }

    if(operaterole||operatelevel){


           sql=getStringNoBlank(sql);
           String primaryKey = rs1.getString("PRIMARYKEY");
           int pageSize = rs1.getInt("PAGESIZE");
		   if(pageSize==0)pageSize=99999;

           while (sql.indexOf("{")>0){
                String param=sql.substring(sql.indexOf("{"),sql.indexOf("}")+1);
                String  p=param.replace("{?","").replace("}","");

                if(p.equals("userid")){
                    sql=sql.replace(param,""+userid);
                }else{
                  sql=sql.replace(param,Util.null2String(request.getParameter(p)));
                }

            }


          //  sql=sql.replace("$userid$",""+userid);

          int pageNo = Util.getIntValue(request.getParameter("pageNo"),1);

          int start =1 +pageSize*(pageNo-1);
          int end   =pageSize +pageSize*(pageNo-1);
          //out.print(sql);
           RecordSet rs =new RecordSet();
           String sqlpage="";

           if ("oracle".equals(rs.getDBType())) {
              sqlpage=" select a.* from (select t.*,rownum rn from  ("+sql+")t) a where rn between "+start+" and "+end ;
            } else  if ("sqlserver".equals(rs.getDBType()))  {
                sql=sql.toUpperCase();
                if(sql.indexOf("SELECT")>=0){
                    sql= sql.replaceFirst("SELECT"," SELECT '0' as overorderid,");
                }
                sqlpage="select * from( select ROW_NUMBER() over(order by overorderid desc) TT,*   FROM ("+sql+")T)TTT WHERE TTT.TT between "+start+" and "+end;
            }
           sqlpage= sqlpage.replace("?"," ");
           sqlpage= sqlpage.replace("？"," ");
           if(debug.equals("1")){
             resultObj.put("dbtype", rs.getDBType());
             resultObj.put("最终执行sql", sqlpage);
             resultObj.put("primaryKey", primaryKey);
           }
           new BaseBean().writeLog("apikey:"+apikey+";userid:"+userid+";exec:"+sqlpage);

                 rs.execute(sqlpage);

                 JSONArray datas = new JSONArray();
                 String[] columnNames = rs.getColumnName();
                 while(rs.next()){
                 	JSONObject jsonObj = new JSONObject();
                 	for(String columnName : columnNames){
                 		jsonObj.put(columnName.toLowerCase(), Util.formatMultiLang(rs.getString(columnName)));
                 	}
                 	datas.add(jsonObj);
                 }



                 resultObj.put("datas", JSONArray.fromObject(datas));
                 resultObj.put("pageSize", pageSize);
                 resultObj.put("pageNo", pageNo);
                 resultObj.put("currentPageSize", datas.size());

                  if(datas.size()==0){
                    resultObj.put("status", -2);
                    resultObj.put("statusinfo", "没有返回值");
                  }
          }else{
            resultObj.put("status", -3);
            resultObj.put("statusinfo", "无权限访问；");
          }

}else{
  resultObj.put("status", -1);
  resultObj.put("statusinfo", "apikey不存在");
}

 if(debug.equals("1")){
    JSONObject paramObj = new JSONObject();
    paramObj.put("入参:pageNo", "第几页，默认值1");
    paramObj.put("入参:apikey", "接口标识");
    paramObj.put("出参:datas", "返回的结果集");
    paramObj.put("出参:currentPageSize", "当前返回的结果集数量");
    paramObj.put("出参:status", "0:正常返回数据；-3:无权限访问；-2:未查询到数据；-1:apikey不存在；");
    resultObj.put("desc", paramObj);
}
out.clearBuffer();
out.print(resultObj.toString());
%>
