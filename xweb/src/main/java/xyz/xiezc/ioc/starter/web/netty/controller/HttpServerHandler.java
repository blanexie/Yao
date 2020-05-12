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
package xyz.xiezc.ioc.starter.web.netty.controller;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

import java.util.Set;
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
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpRequest> {

    Log log = LogFactory.get(HttpServerHandler.class);

    DispatcherHandler dispatcherHandler;

    public HttpServerHandler(DispatcherHandler dispatcherHandler) {
        this.dispatcherHandler = dispatcherHandler;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest httpRequest) {
        CompletableFuture<HttpRequest> future = CompletableFuture.completedFuture(httpRequest);
        Executor executor = ctx.executor();
        future.thenAcceptAsync(request -> {
            try {
                String method = request.getMethod();
                String uri = request.getPath();
                //静态文件， 交给下个handler处理
                if (isStaticFile(method, uri)) {
                    ctx.fireChannelRead(request);
                    return;
                }
                FullHttpResponse fullHttpResponse = dispatcherHandler.doRequest(request);
                writeResponse(ctx, future, fullHttpResponse);
            } catch (Exception e) {
                this.handleException(e);
            }
        }, executor);
    }


    private void writeResponse(ChannelHandlerContext ctx, CompletableFuture<HttpRequest> future, FullHttpResponse msg) {
        ctx.writeAndFlush(msg);
        future.complete(null);
    }

    private FullHttpResponse handleException(Throwable e) {
        log.error(e.getMessage(), e);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(500));
        return response;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!isResetByPeer(cause)) {
            log.error(cause.getMessage(), cause);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(500));
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 非post请求，并且请求的路径不在get请求的controller路径中。 则默认是进行静态文件下载
     */
    private boolean isStaticFile(String method, String path) {
        Set<String> getPaths = DispatcherHandler.getMethods.keySet();
        if (HttpMethod.POST.name().equals(method) || getPaths.contains(path)) {
            return false;
        }
        return true;
    }




    boolean isResetByPeer(Throwable e) {
        if (null != e.getMessage() &&
                e.getMessage().contains("Connection reset by peer")) {
            return true;
        }
        return false;
    }
}