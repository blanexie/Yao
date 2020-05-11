package xyz.xiezc.ioc.starter.web.common;

import cn.hutool.core.exceptions.ExceptionUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

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
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        return response;
    }


}
