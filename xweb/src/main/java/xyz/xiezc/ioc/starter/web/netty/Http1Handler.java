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
package xyz.xiezc.ioc.starter.web.netty;

import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.entity.FileUpload;
import xyz.xiezc.ioc.starter.web.entity.HttpContent;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.entity.HttpResponse;

import java.io.IOException;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HTTP handler that responds with a "Hello World"
 */
public class Http1Handler extends SimpleChannelInboundHandler<FullHttpRequest> {


    static Log log = LogFactory.get(Http1Handler.class);

    public Http1Handler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        if (HttpUtil.is100ContinueExpected(req)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER));
        }

        HttpRequest httpRequest = getHttpRequest(req);

        HttpResponse httpResponse = DispatcherHandler.doRequest(httpRequest);
        xyz.xiezc.ioc.starter.web.enums.HttpResponseStatus httpResponseStatus = httpResponse.getHttpResponseStatus();
        FullHttpResponse response;
        if (httpResponse.getBody() != null) {
            ByteBuf content = ctx.alloc().buffer();
            content.writeCharSequence(JSONUtil.toJsonStr(httpResponse.getBody()), CharsetUtil.UTF_8);
            response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(httpResponseStatus.code()), content);
        } else {
            response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(httpResponseStatus.code()));
        }

        httpResponse.getHeaders().stream().forEach(httpHeader -> {
            response.headers().set(httpHeader.getKey(), httpHeader.getKey());
        });

        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        if (HttpUtil.isKeepAlive(req)) {
            if (req.protocolVersion().equals(HTTP_1_0)) {
                response.headers().set(CONNECTION, KEEP_ALIVE);
            }
            ctx.write(response);
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }


    HttpRequest getHttpRequest(FullHttpRequest request) throws IOException {
        HttpRequest httpRequest = HttpRequest.build(request.uri());
        HttpMethod method = request.method();
        if (method == HttpMethod.GET) {
            httpRequest.setMethod("get");
        }
        if (method == HttpMethod.POST) {
            httpRequest.setMethod("post");

            HttpPostRequestDecoder requestDecoder = new HttpPostRequestDecoder(request);
            List<InterfaceHttpData> bodyHttpDatas = requestDecoder.getBodyHttpDatas();
            for (InterfaceHttpData bodyHttpData : bodyHttpDatas) {
                HttpContent httpContent = new HttpContent();
                httpRequest.addBody(httpContent);
                InterfaceHttpData.HttpDataType httpDataType = bodyHttpData.getHttpDataType();
                if (Objects.equals(httpDataType.name(), HttpContent.HttpDataType.Attribute.name())) {
                    MixedAttribute attribute = ((MixedAttribute) bodyHttpData);
                    httpContent.setName(attribute.getName());//attribute.getName(), attribute.getValue());
                    httpContent.setValue(attribute.getValue());
                    httpContent.setHttpDataType(HttpContent.HttpDataType.Attribute);
                }
                if (Objects.equals(httpDataType.name(), HttpContent.HttpDataType.FileUpload.name())) {
                    MixedFileUpload attribute = ((MixedFileUpload) bodyHttpData);
                    FileUpload fileUpload = new FileUpload();
                    fileUpload.setFileName(attribute.getFilename());
                    fileUpload.setCharset(attribute.getCharset());
                    fileUpload.setByteBuffer(attribute.content().nioBuffer());
                    fileUpload.setContentType(attribute.getContentType());
                    fileUpload.setSize(attribute.getByteBuf().readableBytes());
                    httpContent.setName(attribute.getName());//attribute.getName(), attribute.getValue());
                    httpContent.setFileUpload(fileUpload);
                    httpContent.setHttpDataType(HttpContent.HttpDataType.FileUpload);
                }
            }
        }

        HttpHeaders headers = request.headers();
        headers.forEach(a -> {
            httpRequest.addHeader(a.getKey(), a.getValue());
        });

        return httpRequest;
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause);
        ctx.close();
    }
}
