package xyz.xiezc.ioc.starter.web.common;

import lombok.Data;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;

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

}
