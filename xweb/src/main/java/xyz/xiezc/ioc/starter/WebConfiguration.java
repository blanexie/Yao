package xyz.xiezc.ioc.starter;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.common.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanFactoryPostProcess;
import xyz.xiezc.ioc.starter.eventListener.ApplicationEvent;
import xyz.xiezc.ioc.starter.eventListener.ApplicationListener;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.web.annotation.PostMapping;
import xyz.xiezc.ioc.starter.web.common.XWebProperties;
import xyz.xiezc.ioc.starter.web.entity.RequestDefinition;
import xyz.xiezc.ioc.starter.web.netty.NettyWebServerInitializer;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class WebConfiguration implements BeanFactoryPostProcess, ApplicationListener {

    Log log = LogFactory.get(WebConfiguration.class);

    @Autowire
    DispatcherHandler dispatcherHandler;

    @Override
    public void process(ApplicationContext applicationContext) {
        Collection<BeanDefinition> values = applicationContext.getSingletonBeanDefinitionMap().values();
        for (BeanDefinition beanDefinition : values) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Controller controller = AnnotationUtil.getAnnotation(beanClass, Controller.class);
            if (controller == null) {
                continue;
            }
            String controllerPath = controller.value();
            Method[] publicMethods = ClassUtil.getPublicMethods(beanClass);
            for (Method publicMethod : publicMethods) {
                GetMapping getMapping = publicMethod.getAnnotation(GetMapping.class);
                if (getMapping != null) {
                    RequestDefinition requestDefinition = new RequestDefinition();
                    requestDefinition.setAnnotation(getMapping);
                    requestDefinition.setBeanDefinition(beanDefinition);
                    requestDefinition.setInvokeMethod(publicMethod);
                    requestDefinition.setHttpMethod(HttpMethod.GET);
                    String path = "/" + controllerPath + "/" + getMapping.value();
                    Path of = Path.of(path);
                    requestDefinition.setPath(of.toString());
                }
                PostMapping postMapping = publicMethod.getAnnotation(PostMapping.class);
                if (postMapping != null) {
                    RequestDefinition requestDefinition = new RequestDefinition();
                    requestDefinition.setAnnotation(postMapping);
                    requestDefinition.setBeanDefinition(beanDefinition);
                    requestDefinition.setInvokeMethod(publicMethod);
                    requestDefinition.setHttpMethod(HttpMethod.POST);
                    String path = "/" + controllerPath + "/" + postMapping.value();
                    Path of = Path.of(path);
                    requestDefinition.setPath(of.toString());
                }
            }
        }
    }

    Set<String> set = new HashSet<>() {{
        add(EventNameConstant.loadListenerEvent);
    }};

    @Override
    public Set<String> dealEventName() {
        return set;
    }

    @Override
    public void doExecute(ApplicationEvent applicationEvent) {


    }


    @Override
    public void startWebServer(XWebProperties xWebProperties) throws Exception {
        boolean ssl = xWebProperties.isSsl();
        int port = xWebProperties.getPort();
        String staticPath = xWebProperties.getStaticPath();
        String websocketPath = xWebProperties.getWebsocketPath();

        // Configure SSL.
        final SslContext sslCtx;
        if (ssl) {
            File certChainFile = new File(xWebProperties.getCertChainFilePath());
            File privateKeyFile = new File(xWebProperties.getPrivatekeyFilePath());
            if (!certChainFile.exists() || !privateKeyFile.exists()) {
                throw new RuntimeException("启用了SSL, 请配置证书文件路径");
            }
            sslCtx = SslContextBuilder.forServer(certChainFile, privateKeyFile).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new NettyWebServerInitializer(sslCtx, dispatcherHandler, staticPath, websocketPath));

            Channel ch = b.bind(port).sync().channel();

            log.info("Open your web browser and navigate to " +
                    (ssl ? "https" : "http") + "://127.0.0.1:" + port + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


}

