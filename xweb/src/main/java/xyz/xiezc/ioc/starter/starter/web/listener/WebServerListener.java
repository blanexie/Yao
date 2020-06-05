package xyz.xiezc.ioc.starter.starter.web.listener;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.starter.annotation.EventListener;
import xyz.xiezc.ioc.starter.annotation.Inject;
import xyz.xiezc.ioc.starter.event.ApplicationEvent;
import xyz.xiezc.ioc.starter.event.ApplicationListener;
import xyz.xiezc.ioc.starter.common.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.starter.web.common.XWebProperties;
import xyz.xiezc.ioc.starter.starter.web.WebServerBootstrap;

@EventListener(eventName = {EventNameConstant.XiocEnd})
public class WebServerListener implements ApplicationListener {

    Log log = LogFactory.get(WebServerListener.class);

    @Override
    public int order() {
        return Short.MAX_VALUE;
    }

    @Inject
    WebServerBootstrap webServerBootstrap;

    @Inject
    XWebProperties xWebProperties;

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
        webServerBootstrap.startWebServer(xWebProperties);
    }

}
