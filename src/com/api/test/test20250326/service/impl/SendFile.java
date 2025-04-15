package com.api.test.test20250326.service.impl;

import com.weaverboot.frame.ioc.anno.classAnno.WeaIocReplaceComponent;
import com.weaverboot.frame.ioc.anno.methodAnno.WeaReplaceAfter;
import com.weaverboot.frame.ioc.anno.methodAnno.WeaReplaceBefore;
import weaver.file.ImageFileManager;

import javax.ws.rs.Path;
import java.io.InputStream;

@Path("/workflow/SendFile")
@WeaIocReplaceComponent
public class SendFile {

    @Path("/api/workflow/GetFileDownUrl")
    public void getFileDownUrl(String fileid) throws Exception {
        String serverIp = "oa.test.yytex.com";
        String downUrl = "%s/weaver/weaver.file.FileDownload?fileid=%s&download=1&ddcode=%s";
        //使用泛微对应的该方法
        weaver.docs.docs.util.DesUtils des = new weaver.docs.docs.util.DesUtils();
        String ddcode = 1+ "_" + fileid;

        ddcode = des.encrypt(ddcode);
        String file = String.format(downUrl, serverIp,fileid,ddcode);

//        return String.format(file);

    }

    @Path("/api/workflow/GetFileDownUrl")
    @WeaReplaceBefore(value = "/api/workflow/GetFileDownUrl",order = 1,description = "获取文件下载链接")
    public void getFileDownUrl1(String fileid) throws Exception {
        String serverIp = "oa.test.yytex.com";
        String downUrl = "%s/weaver/weaver.file.FileDownload?fileid=%s&download=1&ddcode=%s";
        //使用泛微对应的该方法
        weaver.docs.docs.util.DesUtils des = new weaver.docs.docs.util.DesUtils();
        String ddcode = 1+ "_" + fileid;

        ddcode = des.encrypt(ddcode);
        String file = String.format(downUrl, serverIp,fileid,ddcode);

//        return String.format(file);

    }

    @WeaReplaceAfter(value = "/api/workflow/GetFile",order = 1,description = "将文件直接发送")
    public InputStream getFile(int imagefileid){
        ImageFileManager imageFileManager = new ImageFileManager();
        imageFileManager.getImageFileInfoById(imagefileid);
        // ImageFileManager  中还有一些其它，方法可以根据需求使用
        InputStream inputStream = imageFileManager.getInputStream();
        return inputStream;
    }
}
