package xyz.xiezc.ioc.starter.web.listener;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.starter.annotation.listener.EventListener;
import xyz.xiezc.ioc.annotation.core.Autowire;
import xyz.xiezc.ioc.eventListener.ApplicationEvent;
import xyz.xiezc.ioc.eventListener.ApplicationListener;
import xyz.xiezc.ioc.common.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.web.common.XWebProperties;
import xyz.xiezc.ioc.starter.web.WebServerBootstrap;

@EventListener(eventName = {EventNameConstant.XiocEnd})
public class WebServerListener implements ApplicationListener {

    Log log = LogFactory.get(WebServerListener.class);

    @Override
    public int order() {
        return Short.MAX_VALUE;
    }

    @Autowire
    WebServerBootstrap webServerBootstrap;

    @Autowire
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
