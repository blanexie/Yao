package xyz.xiezc.ioc.starter.web.common;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class XWebUtil {



    public static FullHttpResponse getErrorResponse(HttpResponseStatus httpResponseStatus, FullHttpRequest httpRequest) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                httpResponseStatus,
                Unpooled.copiedBuffer( httpResponseStatus.toString(), CharsetUtil.UTF_8)
        );

        // Tell the client we're going to close the connection.
        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
        if (keepAlive) {
            if (!httpRequest.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, true);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }



    public static FullHttpResponse getErrorResponse(HttpResponseStatus httpResponseStatus,String errorMsg, FullHttpRequest httpRequest) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                httpResponseStatus,
                Unpooled.copiedBuffer( errorMsg , CharsetUtil.UTF_8)
        );

        // Tell the client we're going to close the connection.

        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
        if (keepAlive) {
            if (!httpRequest.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, true);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }




}
