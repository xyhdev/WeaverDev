<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="weaver.general.*" %>
<%@ page import="com.customization.commons.Console" %>
<%@ page import="weaver.file.ImageFileManager" %>
<%@ page import="java.io.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.zip.*" %>
<%@ page import="weaver.conn.RecordSet" %>

<%!
        public static void downloadPic(int pageNo){

            String logPath = GCONST.getRootPath() + "filesystem"+File.separatorChar +"downloadHrmPic" ;
            File dir = new File(logPath);
            //writeLog(logPath);
            if (!dir.exists()) {
                dir.mkdir();
                System.out.println("创建:"+logPath);
            }
            logPath = GCONST.getRootPath() + "filesystem"+File.separatorChar +"downloadHrmPic"+File.separatorChar +"workPhoto" ;
             dir = new File(logPath);
            //writeLog(logPath);
            if (!dir.exists()) {
                dir.mkdir();
                System.out.println("创建:"+logPath);
            }
            logPath = GCONST.getRootPath() + "filesystem"+File.separatorChar +"downloadHrmPic"+File.separatorChar +"head" ;
             dir = new File(logPath);
            //writeLog(logPath);
            if (!dir.exists()) {
                dir.mkdir();
                System.out.println("创建:"+logPath);
            }

            String path="";
            RecordSet rs= new RecordSet();
            int pageCount= 10000;
            int start =1 +pageCount*(pageNo-1);
            int end   =pageCount +pageCount*(pageNo-1);
            String sql="select  MESSAGERURL,resourceimageid,ID,WORKCODE,LASTNAME,imagefileid,filerealpath,imagefiletype,imagefilename,iszip from HRMRESOURCE,imagefile where resourceimageid=imagefileid and STATUS<=3";
            String sqlpage="select * from( select ROW_NUMBER() over(order by modedatacreatedate desc) TT,*  FROM ("+sql+")T)TTT WHERE TTT.TT between "+start+" and "+end;
            if ("oracle".equals(rs.getDBType())) {
              sqlpage=" select a.* from (select t.*,rownum rn from  ("+sql+")t) a where rn between "+start+" and "+end;
            }
            rs.execute(sqlpage);
            int count=rs.getCounts();
            int i=0;
            while (rs.next()) {

            try {

                path=GCONST.getRootPath()+"filesystem/downloadHrmPic/workPhoto/";

                Console.log("("+(++i)+"/"+count+")"+rs.getString("LASTNAME")+":"+rs.getString("ID"));

                String imagefilename=rs.getString("imagefilename");
                String addr=imagefilename.substring(imagefilename.lastIndexOf("."));
                String filename=rs.getString("LASTNAME").replace(" ","").replace("/","-")+"_"+rs.getString("WORKCODE")+addr;

                String filerealpath = rs.getString("filerealpath");
                String imagefileid = rs.getString("imagefileid");
                int iszip = rs.getInt("iszip");

                new BaseBean().writeLog("iszip==>" + iszip + ",filerealpath=====>" + filerealpath);
                ZipFile zf = null;
                ZipInputStream zin = null;
                ZipEntry ze = null;
                InputStream is = null;
                File file = null;
                BufferedInputStream in=null;
                BufferedOutputStream out=null;



                              if (1 == iszip) {
                                  zf = new ZipFile(filerealpath);
                                  zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(filerealpath)));
                                  ze = zin.getNextEntry();
                                  is = zf.getInputStream(ze);
                              } else {
                                  ImageFileManager imageFileManager = new ImageFileManager();
                                  imageFileManager.getImageFileInfoById(Util.getIntValue(imagefileid, 0));
                                  is = imageFileManager.getInputStream();

                              }

                              in=new BufferedInputStream(is);
                              String p=path+""+new String(filename.getBytes("GBK"));

                              out=new BufferedOutputStream(new FileOutputStream(p));
                              int len=-1;
                              byte[] b=new byte[1024];
                              while((len=in.read(b))!=-1){
                                  out.write(b,0,len);
                              }
                              is.close();
                              in.close();
                              out.close();




                            if(!rs.getString("MESSAGERURL").equals("")){
                                 filerealpath=GCONST.getRootPath()+rs.getString("MESSAGERURL");
                                 in=new BufferedInputStream(new FileInputStream(filerealpath));
                                 String MESSAGERURL=rs.getString("MESSAGERURL");
                                 addr=MESSAGERURL.substring(MESSAGERURL.lastIndexOf("."));
                                 filename=rs.getString("LASTNAME").replace(" ","").replace("/","-")+"_"+rs.getString("WORKCODE")+addr;
                                 path=GCONST.getRootPath()+"filesystem/downloadHrmPic/head/"+filename;
                                 out=new BufferedOutputStream(new FileOutputStream(path));
                                   len=-1;
                                   b=new byte[1024];
                                 while((len=in.read(b))!=-1){
                                     out.write(b,0,len);
                                 }
                                 is.close();
                                 in.close();
                                 out.close();
                         }

              } catch (Exception e) {
                  e.printStackTrace();
                  Console.log(e.toString());
              } finally {


              }
        }
      }
%>
<%
     int pageNo = Util.getIntValue(Util.null2String(request.getParameter("pageNo")),1);
     downloadPic(pageNo);
     out.print("导入完成");
%>
