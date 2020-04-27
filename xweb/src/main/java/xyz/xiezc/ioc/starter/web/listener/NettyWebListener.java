package xyz.xiezc.ioc.starter.web.listener;

import lombok.SneakyThrows;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;

@Component
public class NettyWebListener implements ApplicationListener {


    @Value("server.static.path")
    String staticResource;
    @Value("server.port")
    Integer port;


    @Override
    public int order() {
        return Short.MAX_VALUE;
    }

    /**
     * 启动Jetty服务器
     *
     * @param applicationEvent
     */
    @SneakyThrows
    @Override
    public void doExecute(ApplicationEvent applicationEvent) {


        return;
    }


}
