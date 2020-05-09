package xyz.xiezc.ioc.starter.web.listener;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.EventListener;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.WebServerBootstrap;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.handler.HttpMessageConverter;
import xyz.xiezc.ioc.starter.web.netty.NettyWebServerBootstrap;

import java.util.List;

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
        List<BeanDefinition> beanDefinitions = Xioc.getApplicationContext()
                .getBeanDefinitions(HttpMessageConverter.class);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            HttpMessageConverter httpMessageConverter = beanDefinition.getBean();
            List<ContentType> supportContentType = httpMessageConverter.getSupportContentType();
            for (ContentType contentType : supportContentType) {
                DispatcherHandler.httpMessageConverterMap.put(contentType, httpMessageConverter);
            }
        }
        webServerBootstrap.startWebServer(ssl, port);
    }


}
