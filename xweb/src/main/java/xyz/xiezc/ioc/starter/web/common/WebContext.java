package xyz.xiezc.ioc.starter.web.common;

import cn.hutool.core.thread.ThreadUtil;
import lombok.Data;
import xyz.xiezc.ioc.starter.web.entity.FileUpload;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.entity.HttpResponse;

import java.util.ArrayList;
import java.util.List;


@Data
public class WebContext {


    static ThreadLocal<Object> threadLocal = ThreadUtil.createThreadLocal(true);

    public static WebContext get() {
        Object w = threadLocal.get();
        if (w == null) {
            w = new WebContext();
            threadLocal.set(w);
        }
        return (WebContext) w;
    }

    public static void remove(){
        threadLocal.remove();
    }

    HttpRequest request;

    HttpResponse response;

    List<FileUpload> fileUploads=new ArrayList<>();

}
