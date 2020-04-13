package xyz.xiezc.ioc.starter;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.web.common.MappingHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
@BeanScan(basePackages = {"xyz.xiezc.web"})
public class WebConfiguration {

    /**
     * 路径处理器
     */
    public static final Map<String, MappingHandler> mappingHandlerMap = new HashMap<>();

//
//    public static Server createServer(int port, Resource baseResource) throws Exception {
//        // Create a basic Jetty server object that will listen on port 8080.  Note that if you set this to port 0
//        // then a randomly available port will be assigned that you can either look in the logs for the port,
//        // or programmatically obtain it for use in test cases.
//        Server server = new Server(port);
//
//        // Create the ResourceHandler. It is the object that will actually handle the request for a given file. It is
//        // a Jetty Handler object so it is suitable for chaining with other handlers as you will see in other examples.
//        ResourceHandler resourceHandler = new ResourceHandler();
//
//        // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
//        // In this example it is the current directory but it can be configured to anything that the jvm has access to.
//        resourceHandler.setDirectoriesListed(true);
//        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
//        resourceHandler.setBaseResource(baseResource);
//
//        // Add the ResourceHandler to the server.
//        HandlerList handlers = new HandlerList();
//        handlers.setHandlers(new Handler[]{resourceHandler, new DefaultHandler()});
//        server.setHandler(handlers);
//
//        return server;
//    }
//
//    public static void main(String[] args) throws Exception {
//        int port = 8090;
//        Server server = createServer(port, null);
//        server.start();
//        server.join();
//    }

}

