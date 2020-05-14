package xyz.xiezc.ioc.starter.web.common;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

import java.io.UnsupportedEncodingException;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class XWebUtil {


    public static FullHttpResponse getErrorResponse(XWebException xWebException) {
        int code = xWebException.getCode();
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(code);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                httpResponseStatus,
                Unpooled.copiedBuffer("ErrorMsg: " + ExceptionUtil.stacktraceToString(xWebException) + "\r\n", CharsetUtil.UTF_8)
        );
        // Tell the client we're going to close the connection.
        response.headers().set(CONNECTION, CLOSE);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    public static FullHttpResponse getErrorResponse(XWebException xWebException, HttpRequest httpRequest) {
        int code = xWebException.getCode();
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(code);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                httpResponseStatus,
                Unpooled.copiedBuffer("ErrorMsg: " + ExceptionUtil.stacktraceToString(xWebException) + "\r\n", CharsetUtil.UTF_8)
        );

        // Tell the client we're going to close the connection.
        if (httpRequest.isKeepAlive()) {
            if (!httpRequest.getHttpVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, true);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .setInt(CONTENT_LENGTH, response.content().readableBytes());
        response.retain();
        return response;
    }

    public static String fileExt(String fname) {
        if (StrUtil.isBlank(fname) || fname.indexOf('.') == -1) {
            return null;
        }
        return fname.substring(fname.lastIndexOf('.') + 1);
    }

    public static String mimeType(String fileName) {
        String ext = fileExt(fileName);
        if (null == ext) {
            return null;
        }
        return MimeType.get(ext);
    }


}
