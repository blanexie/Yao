package xyz.xiezc.ioc.starter.web.netty;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.Data;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Init;
import xyz.xiezc.ioc.starter.web.WebServerBootstrap;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

@Data
@Component
public class NettyWebServerBootstrap implements WebServerBootstrap {

    Log log = LogFactory.get(NettyWebServerBootstrap.class);

    @Override
    public void startWebServer(boolean ssl, int port) throws Exception {
        // Configure SSL.
        final SslContext sslCtx = createSslContext(ssl);
        // Configure the server.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new Http2ServerInitializer(sslCtx));

            Channel ch = b.bind(port).sync().channel();
            log.info("Open your HTTP/2-enabled web browser and navigate to " +
                    (ssl ? "https" : "http") + "://127.0.0.1:" + port + '/');
            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    private SslContext createSslContext(boolean ssl) throws CertificateException, SSLException {
        if (ssl) {
            SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                    .sslProvider(provider)
                    /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                     * Please refer to the HTTP/2 specification for cipher requirements. */
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                            ApplicationProtocolConfig.Protocol.NPN_AND_ALPN,
                            // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                            // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1))
                    .build();
            return sslCtx;
        } else {
            return null;
        }

    }

}
