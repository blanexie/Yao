package xyz.xiezc.ioc.starter.starter;

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
import io.netty.handler.ssl.util.SelfSignedCertificate;
import xyz.xiezc.ioc.system.annotation.BeanScan;
import xyz.xiezc.ioc.system.annotation.Configuration;
import xyz.xiezc.ioc.starter.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.starter.web.WebServerBootstrap;
import xyz.xiezc.ioc.starter.starter.web.common.XWebProperties;
import xyz.xiezc.ioc.starter.starter.web.netty.NettyWebServerInitializer;
import xyz.xiezc.ioc.system.annotation.Inject;

@Configuration
@BeanScan(basePackages = {"xyz.xiezc.ioc.starter.web"})
public class WebConfiguration implements WebServerBootstrap {

    Log log = LogFactory.get(WebConfiguration.class);

    @Inject
    DispatcherHandler dispatcherHandler;


    @Override
    public void startWebServer(XWebProperties xWebProperties) throws Exception {
        boolean ssl = xWebProperties.isSsl();
        int port = xWebProperties.getPort();
        String staticPath = xWebProperties.getStaticPath();
        String websocketPath = xWebProperties.getWebsocketPath();

        // Configure SSL.
        final SslContext sslCtx;
        if (ssl) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
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

            System.err.println("Open your web browser and navigate to " +
                    (ssl ? "https" : "http") + "://127.0.0.1:" + port + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

