package xyz.xiezc.web.listener;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.PathResource;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.common.event.Event;
import xyz.xiezc.ioc.common.event.Listener;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.web.common.ContentType;
import xyz.xiezc.web.common.DispatcherHandler;
import xyz.xiezc.web.handler.HttpMessageConverter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class JettyWebListener implements Listener {


    @Value("server.static.path")
    String staticResource;
    @Value("server.port")
    Integer port;

    @Inject
    DispatcherHandler dispatcherHandler;

    @Override
    public int order() {
        return Short.MAX_VALUE;
    }

    /**
     * 启动Jetty服务器
     *
     * @param event
     */
    @SneakyThrows
    @Override
    public void execute(Event event) {

        //加载对应的HttpMessageConverter
        List<BeanDefinition> beanDefinitions = Xioc.getSingleton().getContextUtil().getBeanDefinitions(HttpMessageConverter.class);
        beanDefinitions.stream().map(beanDefinition ->
                (HttpMessageConverter) beanDefinition.getBean()
        ).forEach(httpMessageConverter -> {
            List<ContentType> supportContentType = httpMessageConverter.getSupportContentType();
            supportContentType.forEach(contentType -> {
                dispatcherHandler.httpMessageConverterMap.put(contentType, httpMessageConverter);
            });
        });


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

        server.start();
        server.join();
        return;
    }


    @Override
    public Event getEvent() {
        return new Event("xioc-initAndInjectBeans-end");
    }

}
