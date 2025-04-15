package com.api.test.test20250326.service.impl;

import com.weaverboot.frame.ioc.anno.classAnno.WeaIocReplaceComponent;
import com.weaverboot.frame.ioc.anno.methodAnno.WeaReplaceAfter;
import com.weaverboot.frame.ioc.anno.methodAnno.WeaReplaceBefore;
import weaver.file.ImageFileManager;

import java.io.InputStream;

@WeaIocReplaceComponent
public class GetFile {
    @WeaReplaceBefore(value = "/api/workflow/reqlist/getFileDownUrl",order = 1,description = "获取文件下载链接")
    public String getFileDownUrl(String a) throws Exception {
        String serverIp = "127.0.0.1";
        String fileid = "1925";
        String downUrl = "%s/weaver/weaver.file.FileDownload?fileid=%s&download=1&ddcode=%s";
        //使用泛微对应的该方法
        weaver.docs.docs.util.DesUtils des = new weaver.docs.docs.util.DesUtils();
        String ddcode = 1+ "_" + fileid;

        ddcode = des.encrypt(ddcode);
        String file = String.format(downUrl, serverIp,fileid,ddcode);
        return String.format(file);

    }
    @WeaReplaceAfter(value = "/api/workflow/reqlist/getFile",order = 2,description = "将文件直接发送")
    public InputStream getFile(int imagefileid){
        imagefileid = 1925;
        ImageFileManager imageFileManager = new ImageFileManager();
        imageFileManager.getImageFileInfoById(imagefileid);
        // ImageFileManager  中还有一些其它，方法可以根据需求使用
        InputStream inputStream = imageFileManager.getInputStream();
        return inputStream;
    }
}