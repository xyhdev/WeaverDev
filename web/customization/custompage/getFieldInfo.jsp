<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="java.lang.*" %>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page" />
<%


       String jscode="/*动态生成的字段变量 start*/\n";
       int nodeid = Util.getIntValue(request.getParameter("nodeid"));
       jscode+=" var nodeid='"+nodeid+"';\n";
       rs.execute("select NODENAME from WORKFLOW_NODEBASE where id='"+nodeid+"'");
       if(rs.first()){
          jscode+=" var nodename='"+rs.getString("NODENAME")+"';\n";
       }

       String vcode="\n/*主表取值 start*/\n";
       int formid = Util.getIntValue(request.getParameter("formid"));
       String sql = "select id,fieldname,billid,detailtable,HTMLLABELINFO.LABELNAME,FIELDDBTYPE from workflow_billfield,HTMLLABELINFO where HTMLLABELINFO.INDEXID=workflow_billfield.FIELDLABEL and HTMLLABELINFO.LANGUAGEID=7 and billid = '"+formid+"' order by detailtable desc " ;
       rs.execute(sql);
       while (rs.next()){
           String fieldname = rs.getString("fieldname");
           String detailtable = rs.getString("detailtable");
           String LABELNAME=rs.getString("LABELNAME");
           String FIELDDBTYPE=rs.getString("FIELDDBTYPE");
           if(detailtable==""||detailtable==null){
                jscode +=" var "+fieldname+"=WfForm.convertFieldNameToId('"+fieldname+"'); //主表-"+LABELNAME +"["+FIELDDBTYPE+"]";
                vcode  +=" var "+fieldname+"Get= function(){return WfForm.getFieldValue(WfForm.convertFieldNameToId('"+fieldname+"'))}; //主表-"+LABELNAME +"["+FIELDDBTYPE+"]\n";

           }else{
                detailtable=detailtable.substring(detailtable.indexOf("_dt")+3);
                jscode +=" var dt"+detailtable+"_"+fieldname+"=WfForm.convertFieldNameToId('"+fieldname+"','detail_"+detailtable+"'); //明细表"+detailtable+"-"+LABELNAME+"["+FIELDDBTYPE+"]";

                vcode  +=" var dt"+detailtable+"_"+fieldname+"Get= function(params){return WfForm.getFieldValue(WfForm.convertFieldNameToId('"+fieldname+"','detail_"+detailtable+"')+'_'+params)}; // 明细"+detailtable+"-"+LABELNAME +"["+FIELDDBTYPE+"]\n";

           }
           jscode +="\n";
       }
        jscode+="/*动态生成的字段变量 end*/\n";
        jscode+=vcode;
        jscode+="/*主表取值 end*/\n";
        jscode+=" if(window.console) {console.log(\"%c更多Ecology二次开发实例访问 https://gitee.com/L1uTaihong/WeaverEc9CustomDev\",\"color:blue\");} //2020/9/21 上午10:52 lth    end   \n";
        jscode+=" if(window.console) {console.log(\"%c更多表单API访问 https://e-cloudstore.com/doc.html\",\"color:blue\");} //2020/9/21 上午10:52 lth    end   \n";
        out.clearBuffer();
        out.flush();
        out.print(jscode);

%>
