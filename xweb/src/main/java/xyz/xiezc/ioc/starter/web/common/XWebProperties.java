package xyz.xiezc.ioc.starter.web.common;

import lombok.Data;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Init;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.netty.websocket.WebSocketFrameHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Data
public class XWebProperties {

    @Value("xweb.server.ssl.enable")
    boolean ssl = true;
    @Value("xweb.server.port")
    int port = 8443;
    @Value("xweb.static.path")
    String staticPath = "/static";
    @Value("xweb.websocket.path")
    String websocketPath = "/websocket";

    @Inject
    DispatcherHandler dispatcherHandler;

    Map<String, WebSocketFrameHandler> webSocketFrameHandlerMap;
    @Init
    public void init() {
        List<BeanDefinition> beanDefinitions =
                Xioc.getApplicationContext().getBeanDefinitions(WebSocketFrameHandler.class);
        webSocketFrameHandlerMap = new HashMap<>();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            webSocketFrameHandlerMap.put(beanDefinition.getBeanName(), beanDefinition.getBean());
        }
    }

}
