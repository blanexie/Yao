/**
 * Copyright (c) 2018, biezhi 王爵 nice (biezhi.me@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.xiezc.ioc.starter.web.netty;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.common.XWebException;
import xyz.xiezc.ioc.starter.web.common.XWebUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Http Server Handler
 *
 * @author biezhi
 * 2018/10/15
 */
@ChannelHandler.Sharable
public class HttpServerHandler extends  ChannelInboundHandlerAdapter {

    Log log = LogFactory.get(HttpServerHandler.class);

    DispatcherHandler dispatcherHandler;

    public HttpServerHandler(DispatcherHandler dispatcherHandler) {
        this.dispatcherHandler = dispatcherHandler;
    }

    @Override
    public void channelRead (ChannelHandlerContext ctx,  Object msg) {
        FullHttpRequest httpRequest=(FullHttpRequest)msg;
        log.info("进入HttpServerHandler：{}", httpRequest.uri());
        CompletableFuture<FullHttpRequest> future = CompletableFuture.completedFuture(httpRequest);
        Executor executor = ctx.executor();
        future.thenAcceptAsync(request -> {
            try {
                FullHttpResponse fullHttpResponse = dispatcherHandler.doRequest(request);
                writeResponse(ctx, future, fullHttpResponse);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                FullHttpResponse errorResponse = XWebUtil.getErrorResponse(new XWebException(e), httpRequest);
                writeResponse(ctx, future, errorResponse);
            }
        }, executor);
    }


    private void writeResponse(ChannelHandlerContext ctx, CompletableFuture<FullHttpRequest> future, FullHttpResponse msg) {
        ctx.writeAndFlush(msg);
        future.complete(null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        if (!isResetByPeer(cause)) {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(StrUtil.bytes(HttpResponseStatus.NOT_FOUND.toString()));
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,byteBuf);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }



    boolean isResetByPeer(Throwable e) {
        if (null != e.getMessage() &&
                e.getMessage().contains("Connection reset by peer")) {
            return true;
        }
        return false;
    }
}