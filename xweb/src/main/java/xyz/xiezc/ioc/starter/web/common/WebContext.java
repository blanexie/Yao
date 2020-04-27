package xyz.xiezc.web.common;

import cn.hutool.core.thread.ThreadUtil;
import lombok.Data;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

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

    ServletRequest request;

    ServletResponse response;

    HttpSession httpSession;

}
