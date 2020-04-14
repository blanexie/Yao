package xyz.xiezc.ioc.starter;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.PathResource;
import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.web.common.DispatcherHandler;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@BeanScan(basePackages = {"xyz.xiezc.web"})
public class WebConfiguration {



    @Value("server.static.path")
    String staticResource;
    @Value("server.port")
    Integer port;

    @Inject
    DispatcherHandler dispatcherHandler;

    public Server createServer() {
        if (port == null) {
            port = 8080;
        }
        if (StrUtil.isBlank(staticResource)) {
            staticResource = "webapp";
        }

        Server server = new Server(port);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});

        Path userDir = Paths.get(ClassUtil.getClassPath() + staticResource).normalize();
        PathResource baseResource = new PathResource(userDir);
        resourceHandler.setBaseResource(baseResource);

        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler);

        handlers.addHandler(dispatcherHandler);

        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);

        return server;
    }


    public static void main(String[] args) throws Exception {
        WebConfiguration webConfiguration = new WebConfiguration();
        Server server = webConfiguration.createServer();
        server.start();
        server.join();
    }

}

