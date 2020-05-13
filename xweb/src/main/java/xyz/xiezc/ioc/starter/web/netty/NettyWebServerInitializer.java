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
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.Data;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.netty.controller.HttpServerHandler;
import xyz.xiezc.ioc.starter.web.netty.file.HttpStaticFileServerHandler;
import xyz.xiezc.ioc.starter.web.netty.websocket.WebSocketFrameHandler;
import xyz.xiezc.ioc.starter.web.netty.websocket.WebSocketServerHandler;

import java.util.Map;

@Data
public class NettyWebServerInitializer extends ChannelInitializer<SocketChannel> {

    private String WEBSOCKET_PATH;

    private SslContext sslCtx;
    private HttpServerHandler httpServerHandler;
    public HttpStaticFileServerHandler httpStaticFileServerHandler;
    public ParseRequestHandler parseRequestHandler;

    Map<String, WebSocketFrameHandler> webSocketFrameHandlerMap;

    public NettyWebServerInitializer(SslContext sslCtx,
                                     DispatcherHandler dispatcherHandler,
                                     String staticPath,
                                     String webSocketPath,
                                     Map<String, WebSocketFrameHandler> webSocketFrameHandlerMap
    ) {
        this.sslCtx = sslCtx;
        this.httpServerHandler = new HttpServerHandler(dispatcherHandler);
        httpStaticFileServerHandler = new HttpStaticFileServerHandler(staticPath);
        parseRequestHandler = new ParseRequestHandler();
        WEBSOCKET_PATH = webSocketPath;
        this.webSocketFrameHandlerMap = webSocketFrameHandlerMap;
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

        //100状态码处理
        pipeline.addLast(new HttpServerExpectContinueHandler());
        //拼装FullHttpRequest
        pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        //WebSocket
        pipeline.addLast(new WebSocketServerHandler(webSocketFrameHandlerMap, sslCtx != null));

        //压缩
        pipeline.addLast(new HttpContentCompressor());
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

    }
}
