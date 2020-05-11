package xyz.xiezc.ioc.starter.web.entity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.util.CharsetUtil;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class HttpRequest {

    private static final ByteBuf EMPTY_BUF = Unpooled.copiedBuffer("", CharsetUtil.UTF_8);

    /**
     * netty的request
     */
    io.netty.handler.codec.http.HttpRequest nettyHttpRequest;

    String path;

    String method;

    String contentType;

    HttpVersion httpVersion;

    boolean keepAlive;

    String remoteAddress;

    List<HttpContent> contents = new LinkedList<>();

    /**
     * 请求地址中的参数
     */
    Map<String, List<String>> queryParamMap;

    /**
     * body中的参数
     */
    Map<String, List<String>> bodyParamMap;

    /**
     * 上传的文件
     */
    Map<String, FileItem> fileItems;


    Set<Cookie> cookies;

    private ByteBuf body = EMPTY_BUF;

    boolean mergeSuccess = false;


    public boolean appendContent(HttpContent httpContent) {
        return contents.add(httpContent);
    }

    public void setNettyHttpRequest(io.netty.handler.codec.http.HttpRequest nettyHttpRequest) {
        this.nettyHttpRequest = nettyHttpRequest;
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(nettyHttpRequest.uri());
        queryParamMap = queryStringDecoder.parameters();
        path = queryStringDecoder.path();
        HttpMethod method = nettyHttpRequest.method();
        this.method = method.name();
        this.httpVersion = nettyHttpRequest.protocolVersion();
        this.keepAlive = HttpUtil.isKeepAlive(nettyHttpRequest);
    }

}
