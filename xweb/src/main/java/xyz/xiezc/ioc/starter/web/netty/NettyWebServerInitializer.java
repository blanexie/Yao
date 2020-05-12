/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package xyz.xiezc.ioc.starter.web.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.netty.controller.HttpServerHandler;
import xyz.xiezc.ioc.starter.web.netty.file.HttpStaticFileServerHandler;

public class NettyWebServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/websocket";

    private final SslContext sslCtx;
    HttpServerHandler httpServerHandler;

    public HttpStaticFileServerHandler httpStaticFileServerHandler;
    public ParseRequestHandler parseRequestHandler;

    public NettyWebServerInitializer(SslContext sslCtx, String staticPath) {
        this.sslCtx = sslCtx;
        BeanDefinition beanDefinition = Xioc.getApplicationContext().getBeanDefinition(DispatcherHandler.class);
        httpServerHandler = new HttpServerHandler(beanDefinition.getBean());
        httpStaticFileServerHandler = new HttpStaticFileServerHandler(staticPath);
        parseRequestHandler = new ParseRequestHandler();
    }


    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }

        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        //编码
        pipeline.addLast(new HttpServerCodec());
        //压缩
        // pipeline.addLast(new HttpContentCompressor());
        //100状态码处理
        pipeline.addLast(new HttpServerExpectContinueHandler());
        //拼装FullHttpRequest
        pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        //处理分块协议
        pipeline.addLast(new ChunkedWriteHandler());
        //防止cors
        pipeline.addLast(new CorsHandler(CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build()));
        //解析FullRequest成HttpRequest
        pipeline.addLast(parseRequestHandler);
        //业务逻辑
        pipeline.addLast(httpServerHandler);
        //静态文件下载
        pipeline.addLast(httpStaticFileServerHandler);

        //WebSocket
//        pipeline.addLast(new WebSocketServerCompressionHandler());
//        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
//        pipeline.addLast(new WebSocketIndexPageHandler(WEBSOCKET_PATH));
//        pipeline.addLast(new WebSocketFrameHandler());

    }
}
