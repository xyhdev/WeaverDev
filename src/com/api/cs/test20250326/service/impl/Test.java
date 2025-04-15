package com.api.cs.test20250326.service.impl;

import com.weaverboot.frame.ioc.anno.classAnno.WeaIocReplaceComponent;
import com.weaverboot.frame.ioc.anno.methodAnno.WeaReplaceAfter;
import com.weaverboot.frame.ioc.anno.methodAnno.WeaReplaceBefore;
import com.weaverboot.frame.ioc.handler.replace.weaReplaceParam.impl.WeaAfterReplaceParam;
import com.weaverboot.frame.ioc.handler.replace.weaReplaceParam.impl.WeaBeforeReplaceParam;
import com.weaverboot.tools.logTools.LogTools;


@WeaIocReplaceComponent
public class Test {
    @WeaReplaceBefore(value = "/api/workflow/reqlist/getFileDownUrl1",order = 1,description = "测试拦截前置")
    public void beforeTest(WeaBeforeReplaceParam weaBeforeReplaceParam) throws Exception {
        weaBeforeReplaceParam.getParamMap().get("");
        String serverIp = "127.0.0.1";
        String fileid = "1925";
        String downUrl = "%s/weaver/weaver.file.FileDownload?fileid=%s&download=1&ddcode=%s";
        //使用泛微对应的该方法
        weaver.docs.docs.util.DesUtils des = new weaver.docs.docs.util.DesUtils();
        String ddcode = 1+ "_" + fileid;

        ddcode = des.encrypt(ddcode);
        String file = String.format(downUrl, serverIp,fileid,ddcode);
    }
    @WeaReplaceAfter(value = "/api/workflow/reqlist/splitPageKey2",order = 1,description = "测试拦截后置")
    public String after(WeaAfterReplaceParam weaAfterReplaceParam){
        String data = weaAfterReplaceParam.getData();//这个就是接口执行完的报文
        LogTools.info("after:/api/workflow/reqlist/splitPageKey");
//        LogTools.info(data);
        return data;
    }
}
