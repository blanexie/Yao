package xyz.xiezc.ioc.starter.starter.web.common;

import lombok.Data;
import xyz.xiezc.ioc.system.Xioc;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.system.annotation.Init;
import xyz.xiezc.ioc.system.annotation.Inject;
import xyz.xiezc.ioc.system.annotation.Value;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.starter.web.netty.websocket.WebSocketFrameHandler;

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

}