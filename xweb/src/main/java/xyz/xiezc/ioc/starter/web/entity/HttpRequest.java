package xyz.xiezc.ioc.starter.web.entity;

import cn.hutool.core.util.StrUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class HttpRequest {


    /**
     * netty的request
     */
    FullHttpRequest fullHttpRequest;

    String path;

    String method;

    String contentType;

    Map<String, List<String>> queryParamMap;

    /**
     *
     */
    String httpVersion;


    public void HttpRequest(FullHttpRequest fullHttpRequest) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());
        queryParamMap = queryStringDecoder.parameters();
        path = queryStringDecoder.path();
        HttpMethod method = fullHttpRequest.method();
        this.method = method.name();
        HttpVersion httpVersion = fullHttpRequest.protocolVersion();
        this.httpVersion = httpVersion.text();
    }

}
