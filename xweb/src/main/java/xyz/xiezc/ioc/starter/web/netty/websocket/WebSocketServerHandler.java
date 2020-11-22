/*
 * Copyright 2014 The Netty Project
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
package xyz.xiezc.ioc.starter.web.netty.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    Log log = LogFactory.get(WebSocketServerHandler.class);
    private Map<String, WebSocketFrameHandler> webSocketFrameHandlerMap;

    boolean isSsl;

    private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR_KEY =
            AttributeKey.valueOf(WebSocketServerHandshaker.class, "HANDSHAKER");
    private static final AttributeKey<WebSocketFrameHandler> WebSocketFrameHandler_ATTR_KEY =
            AttributeKey.valueOf(WebSocketFrameHandler.class, "WebSocketFrameHandler");

    public WebSocketServerHandler(Map<String, WebSocketFrameHandler> webSocketFrameHandlerMap, boolean isSsl) {
        this.webSocketFrameHandlerMap = webSocketFrameHandlerMap;
        this.isSsl = isSsl;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            log.info("进入HttpServerHandler：{}", ((FullHttpRequest) msg).uri());
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            log.info("进入HttpServerHandler1：{}");
            Attribute<WebSocketFrameHandler> attr = ctx.channel().attr(WebSocketFrameHandler_ATTR_KEY);
            WebSocketFrameHandler webSocketFrameHandler = attr.get();
            WebSocketFrame webSocketFrame = webSocketFrameHandler.handle((WebSocketFrame) msg);
            // Check for closing frame
            if (webSocketFrame instanceof CloseWebSocketFrame) {
                Attribute<WebSocketServerHandshaker> attr1 = ctx.channel().attr(HANDSHAKER_ATTR_KEY);
                attr1.get().close(ctx.channel(), (CloseWebSocketFrame) webSocketFrame);
                return;
            }
            //other frame
            ctx.write(webSocketFrame);
            return;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(req.protocolVersion(), BAD_REQUEST,
                    ctx.alloc().buffer(0)));
            return;
        }

        // Allow only GET methods.
        if (!GET.equals(req.method())) {
            ReferenceCountUtil.retain(req);
            ctx.fireChannelRead(req);
            return;
        }
        //根据请求头判断是否是升级websocket的请求
        String connection = req.headers().get("Connection");
        String upgrade = req.headers().get("Upgrade");
        if (StrUtil.equalsIgnoreCase(connection, "Upgrade") && StrUtil.equalsIgnoreCase(upgrade, "websocket")) {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
            //判断请求的路径是否存在
            WebSocketFrameHandler webSocketFrameHandler = webSocketFrameHandlerMap.get(queryStringDecoder.path());
            if (webSocketFrameHandler == null) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(req.protocolVersion(), NOT_FOUND,
                        ctx.alloc().buffer(0)));
                return;
            }
            //请求的路径存在
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req, queryStringDecoder.path()), null, true, 5 * 1024 * 1024);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                return;
            } else {
                handshaker.handshake(ctx.channel(), req);
                ctx.channel().attr(WebSocketFrameHandler_ATTR_KEY).set(webSocketFrameHandler);
                ctx.channel().attr(HANDSHAKER_ATTR_KEY).set(handshaker);
                return;
            }
        }
        //走到这里说明不是websocket协议
        ReferenceCountUtil.retain(req);
        ctx.fireChannelRead(req);
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        HttpResponseStatus responseStatus = res.status();
        if (responseStatus.code() != 200) {
            ByteBufUtil.writeUtf8(res.content(), responseStatus.toString());
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // Send the response and close the connection if necessary.
        boolean keepAlive = HttpUtil.isKeepAlive(req) && responseStatus.code() == 200;
        HttpUtil.setKeepAlive(res, keepAlive);
        ChannelFuture future = ctx.write(res); // Flushed in channelReadComplete()
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private String getWebSocketLocation(FullHttpRequest req, String path) {
        String location = req.headers().get(HttpHeaderNames.HOST) + path;
        if (isSsl) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }
}
