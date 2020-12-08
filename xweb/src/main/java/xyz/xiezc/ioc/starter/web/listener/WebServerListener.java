package xyz.xiezc.ioc.starter.web.listener;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.common.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.eventListener.ApplicationEvent;
import xyz.xiezc.ioc.starter.eventListener.ApplicationListener;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.netty.NettyWebServerInitializer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * @author xiezc
 */
@Component
public class WebServerListener implements ApplicationListener {

    Log log = LogFactory.get(WebServerListener.class);

    Set<String> set = new HashSet<>() {{
        add(EventNameConstant.loadListenerEvent);
    }};

    @Autowire
    DispatcherHandler dispatcherHandler;
    @Autowire
    ApplicationContext applicationContext;

    @Override
    public Set<String> dealEventName() {
        return set;
    }

    @Override
    public boolean getSync() {
        return false;
    }

    @Override
    public void doExecute(ApplicationEvent applicationEvent) {
        this.startWebServer( );
    }

    @SneakyThrows
    private void startWebServer( ) {
        String sslEnable = applicationContext.getProperty("server.ssl.enable");
        String port = applicationContext.getProperty("server.port");
        // Configure SSL.
        final SslContext sslCtx;
        if (StrUtil.equalsIgnoreCase(sslEnable,"true")) {
            String certChainFilePath = applicationContext.getProperty("server.ssl.certChainFilePath");
            File certChainFile = new File(certChainFilePath);
            String privateKeyFilePath = applicationContext.getProperty("server.ssl.privateKeyFilePath");
            File privateKeyFile = new File(privateKeyFilePath);
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
                    .childHandler(new NettyWebServerInitializer(sslCtx, dispatcherHandler));
            Channel ch = b.bind(Convert.toInt(port,8016)).sync().channel();

            log.info("Open your web browser and navigate to " +
                    (StrUtil.equalsIgnoreCase(sslEnable,"true") ? "https" : "http") + "://127.0.0.1:" + port + '/');
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
