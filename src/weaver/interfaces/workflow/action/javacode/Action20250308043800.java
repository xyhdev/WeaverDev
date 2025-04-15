package weaver.interfaces.workflow.action.javacode;

import weaver.conn.RecordSet;
import weaver.file.ImageFileManager;
import weaver.interfaces.workflow.action.Action;
import weaver.general.BaseBean;
import weaver.soa.workflow.request.RequestInfo;

import java.io.InputStream;

/**
 * Online custom action interface
 */
public class Action20250308043800 extends BaseBean implements Action{

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
        int fileid = mainrs.getInt("xgfj");

        try {
            String FileDownUrl = getFileDownUrl(fileid);
            rse.execute("update " + tablename + " set jg='" + FileDownUrl + "' where requestId=" + requestId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Action.SUCCESS;
    }

    public String getFileDownUrl(int fileid) throws Exception {
        String serverIp = "172.16.14.153";
        String downUrl = "%s/weaver/weaver.file.FileDownload?fileid=%s&download=1&ddcode=%s";
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
