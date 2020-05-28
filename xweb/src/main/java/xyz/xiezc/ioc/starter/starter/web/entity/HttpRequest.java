package xyz.xiezc.ioc.starter.starter.web.entity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.util.CharsetUtil;
import lombok.Data;

import java.util.*;

@Data
public class HttpRequest {

    private static final ByteBuf EMPTY_BUF = Unpooled.copiedBuffer("", CharsetUtil.UTF_8);

    /**
     * netty的request
     */
    FullHttpRequest nettyHttpRequest;

    String path;

    String method;

    String contentType;

    HttpVersion httpVersion;

    boolean keepAlive;

    /**
     * 请求地址中的参数
     */
    Map<String, List<String>> queryParamMap = new HashMap<>();

    /**
     * body中的参数
     */
    Map<String, List<String>> bodyParamMap = new HashMap<>();

    /**
     * 上传的文件
     */
    Map<String, FileItem> fileItems = new HashMap<>();

    /**
     * cookie 信息
     */
    Set<Cookie> cookies = new HashSet<>();

    /**
     * body
     */
    private ByteBuf body = EMPTY_BUF;

}
