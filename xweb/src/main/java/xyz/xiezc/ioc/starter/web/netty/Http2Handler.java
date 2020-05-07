/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package xyz.xiezc.ioc.starter.web.netty;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.entity.HttpContent;
import xyz.xiezc.ioc.starter.web.entity.HttpHeader;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.entity.HttpResponse;

import java.util.List;

/**
 * A simple handler that responds with the message "Hello World!".
 */
public final class Http2Handler extends Http2ConnectionHandler implements Http2FrameListener {

    Http2Handler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                 Http2Settings initialSettings) {
        super(decoder, encoder, initialSettings);
    }

    private static Http2Headers http1HeadersToHttp2Headers(FullHttpRequest request) {
        CharSequence host = request.headers().get(HttpHeaderNames.HOST);
        Http2Headers http2Headers = new DefaultHttp2Headers()
                .method(HttpMethod.GET.asciiName())
                .path(request.uri())
                .scheme(HttpScheme.HTTP.name());
        if (host != null) {
            http2Headers.authority(host);
        }
        return http2Headers;
    }

    /**
     * Handles the cleartext HTTP upgrade event. If an upgrade occurred, sends a simple response via HTTP/2
     * on stream 1 (the stream specifically reserved for cleartext HTTP upgrade).
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
            HttpServerUpgradeHandler.UpgradeEvent upgradeEvent =
                    (HttpServerUpgradeHandler.UpgradeEvent) evt;
            onHeadersRead(ctx, 1, http1HeadersToHttp2Headers(upgradeEvent.upgradeRequest()), 0, true);
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * Sends a "Hello World" DATA frame to the client.
     */
    private void sendResponse(ChannelHandlerContext ctx, int streamId, ByteBuf payload, Http2Headers headers) {
        // Send a frame for the response status

        encoder().writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise());
        encoder().writeData(ctx, streamId, payload, 0, true, ctx.newPromise());

        // no need to call flush as channelReadComplete(...) will take care of it.
    }

    @SneakyThrows
    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) {
        ctx.pipeline().addLast(new HttpObjectAggregator(Http2ServerInitializer.maxHttpContentLength));
        int processed = data.readableBytes() + padding;

        HttpRequest httpRequest = DispatcherHandler.requestCache.get(streamId);
        if (httpRequest == null) {
            Http2Headers respHeaders = new DefaultHttp2Headers().status(HttpResponseStatus.REQUEST_TIMEOUT.codeAsText());
            sendResponse(ctx, streamId, data.retain(), respHeaders);
            return processed;
        }
        //TODO   httpRequest.addBody(charSequence.toString());
        if (endOfStream) {
            HttpResponse httpResponse = DispatcherHandler.doRequest(httpRequest);
            sendResponse(ctx, streamId, httpResponse);
//            HttpResponse httpResponse = DispatcherHandler.doRequest(httpRequest);
//            dealResonse(ctx, streamId, httpResponse);
        }
        return processed;
    }

    private void sendResponse(ChannelHandlerContext ctx, int streamId, HttpResponse httpResponse) {
        ByteBuf data = ctx.alloc().buffer();
        List<HttpHeader> headers = httpResponse.getHeaders();
        Http2Headers respHeaders = new DefaultHttp2Headers();
        headers.forEach(httpHeader -> {
            respHeaders.add(httpHeader.getKey(), httpHeader.getValue());
        });
        respHeaders.status(HttpResponseStatus.OK.codeAsText());
        Object body = httpResponse.getBody();
        data.clear();
        data.writeCharSequence(JSONUtil.toJsonStr(body), CharsetUtil.UTF_8);
        sendResponse(ctx, streamId, data, respHeaders);
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId,
                              Http2Headers headers, int padding, boolean endOfStream) {


        HttpRequest httpRequest = getHttpRequest(headers);
        httpRequest.setReqId(streamId);
        if (endOfStream) {
            HttpResponse httpResponse = DispatcherHandler.doRequest(httpRequest);
            dealResonse(ctx, streamId, httpResponse);
        } else {
            DispatcherHandler.headerCache.put(streamId, headers);
        }
    }

    private void dealResonse(ChannelHandlerContext ctx, int streamId, HttpResponse httpResponse) {
        Http2Headers respHeaders = new DefaultHttp2Headers().status(httpResponse.getHttpResponseStatus().code() + "");
        if (httpResponse.getHeaders() != null && !httpResponse.getHeaders().isEmpty()) {
            httpResponse.getHeaders().stream().forEach(httpHeader -> {
                respHeaders.add(httpHeader.getKey(), httpHeader.getValue());
            });
        }
        Object body = httpResponse.getBody();
        String s = JSONUtil.toJsonStr(body);
        ByteBuf content = ctx.alloc().buffer();
        content.writeCharSequence(s, CharsetUtil.UTF_8);
        DispatcherHandler.requestCache.remove(streamId);
        sendResponse(ctx, streamId, content, respHeaders);
    }

    private HttpRequest getHttpRequest(Http2Headers headers) {
        CharSequence method = headers.method();
        HttpRequest httpRequest = HttpRequest.build(headers.path().toString());
        httpRequest.setMethod(method.toString());
        headers.forEach(a -> {
            httpRequest.addHeader(a.getKey().toString(), a.getValue().toString());
        });
        return httpRequest;
    }


    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                              short weight, boolean exclusive, int padding, boolean endOfStream) {
        onHeadersRead(ctx, streamId, headers, padding, endOfStream);
    }

    @Override
    public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency,
                               short weight, boolean exclusive) {
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) {
    }

    @Override
    public void onSettingsAckRead(ChannelHandlerContext ctx) {
    }

    @Override
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) {
    }

    @Override
    public void onPingRead(ChannelHandlerContext ctx, long data) {
    }

    @Override
    public void onPingAckRead(ChannelHandlerContext ctx, long data) {
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                  Http2Headers headers, int padding) {
    }

    @Override
    public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) {
    }

    @Override
    public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) {
    }

    @Override
    public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType, int streamId,
                               Http2Flags flags, ByteBuf payload) {
    }
}
