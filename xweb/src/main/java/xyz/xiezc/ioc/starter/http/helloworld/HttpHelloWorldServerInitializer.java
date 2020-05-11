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
package xyz.xiezc.ioc.starter.http.helloworld;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.http.file.HttpStaticFileServerHandler;
import xyz.xiezc.ioc.starter.http.upload.HttpUploadServerHandler;
import xyz.xiezc.ioc.starter.http.websocketx.server.WebSocketFrameHandler;
import xyz.xiezc.ioc.starter.http.websocketx.server.WebSocketIndexPageHandler;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.HttpServerHandler;
import xyz.xiezc.ioc.starter.web.MergeRequestHandler;

public class HttpHelloWorldServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/websocket";

    private final SslContext sslCtx;
    DispatcherHandler dispatcherHandler;

    public HttpHelloWorldServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
        BeanDefinition beanDefinition = Xioc.getApplicationContext().getBeanDefinition(DispatcherHandler.class);
        dispatcherHandler = beanDefinition.getBean();
    }


    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }

        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        pipeline.addLast(new HttpServerCodec());
        //100状态码的处理
        pipeline.addLast(new HttpServerExpectContinueHandler());
        //支持压缩
        pipeline.addLast(new HttpContentCompressor());
        //cors
        pipeline.addLast(new CorsHandler(CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build()));
        pipeline.addLast(new MergeRequestHandler());

        //正常的请求
        pipeline.addLast(new HttpServerHandler(dispatcherHandler));

        //文件下载
        pipeline.addLast(new HttpStaticFileServerHandler());



        //WebSocket
//        pipeline.addLast(new WebSocketServerCompressionHandler());
//        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
//        pipeline.addLast(new WebSocketIndexPageHandler(WEBSOCKET_PATH));
//        pipeline.addLast(new WebSocketFrameHandler());

    }
}
