package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.file.ImageFileManager;
import weaver.soa.workflow.request.RequestInfo;

import java.io.InputStream;

public class GetFileAction implements Action {

    /**
     * 流程路径节点后选择aciton后,会在节点提交后执行此方法。
     */
    @Override
    public String execute(RequestInfo request) {
        RecordSet rse = new RecordSet();
        // 获取requestid
        String requestId = request.getRequestid();
        // 获取表单名称
        String tablename = request.getRequestManager().getBillTableName();
        // 查找表单相关内容
        RecordSet mainrs = new RecordSet();
        mainrs.execute("select * from "+tablename+" where requestid =  "+requestId);
        mainrs.next();
        // 获取相关附件
        int xgfj = mainrs.getInt("xgfj");
        mainrs.execute("select * from DocImageFile where docid = "+xgfj);
        mainrs.next();
        int fileid = mainrs.getInt("imagefileid");
        try {
            String FileDownUrl = getFileDownUrl(fileid);
            rse.execute("update " + tablename + " set fjdz='" + FileDownUrl + "' where requestId=" + requestId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Action.SUCCESS;
    }

    public String getFileDownUrl(int fileid) throws Exception {
        String serverIp = "192.168.7.12:8088";
        String downUrl = "http://%s/weaver/weaver.file.FileDownload?fileid=%s&download=1&ddcode=%s";
        //使用泛微对应的该方法
        weaver.docs.docs.util.DesUtils des = new weaver.docs.docs.util.DesUtils();
        String ddcode = 1+ "_" + fileid;

        ddcode = des.encrypt(ddcode);
        String FileDownUrl = String.format(downUrl, serverIp,fileid,ddcode);
        return FileDownUrl;
    }

    public ImageFileManager getFile(int imagefileid){
        ImageFileManager imageFileManager = new ImageFileManager();
        imageFileManager.getImageFileInfoById(imagefileid);
        // ImageFileManager  中还有一些其它，方法可以根据需求使用
        InputStream inputStream = imageFileManager.getInputStream();
        return imageFileManager;
    }

}
