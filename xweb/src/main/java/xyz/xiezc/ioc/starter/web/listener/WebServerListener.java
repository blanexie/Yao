package xyz.xiezc.ioc.starter.web.listener;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.annotation.EventListener;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.web.WebServerBootstrap;

@EventListener(eventName = {EventNameConstant.XiocEnd})
public class WebServerListener implements ApplicationListener {

    Log log = LogFactory.get(WebServerListener.class);

    @Override
    public int order() {
        return Short.MAX_VALUE;
    }

    @Inject
    WebServerBootstrap webServerBootstrap;

    @Value("xweb.server.ssl.enable")
    boolean ssl = true;
    @Value("xweb.server.port")
    int port = 8443;
    @Value("xweb.static.path")
    String staticPath = "/static";

    @Override
    public boolean getSync() {
        return false;
    }

    /**
     * 启动netty服务器
     *
     * @param applicationEvent
     */
    @SneakyThrows
    @Override
    public void doExecute(ApplicationEvent applicationEvent) {
        log.info("正在启动web服务.........");
        webServerBootstrap.startWebServer(ssl, port, staticPath);
    }

}
