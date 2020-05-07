package xyz.xiezc.ioc.starter.web.entity;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class HttpRequest {

    Integer reqId;

    String path;

    String method;

    String cookie;

    String contentType;

    String charset;

    List<HttpHeader> headers = new ArrayList<>();

    Map<String, List<String>> paramMap;

    List<HttpContent> bodyList = new ArrayList<>();

    /**
     * 是否是HTTP2应用
     */
    boolean isHttp2;


    public void addBody(HttpContent content) {
        bodyList.add(content);
    }

    public void addHeader(String key, String value) {
        headers.add(new HttpHeader(key, value));
    }

    public static HttpRequest build(String queryStr) {
        HttpRequest httpRequest = new HttpRequest();
        Map<String, List<String>> reqParms = new HashMap<>();
        if (queryStr.contains("?")) {
            String[] split = StrUtil.split(queryStr, "?");
            httpRequest.setPath(split[0]);
            String parmsStr = split[1];
            reqParms = parseParams(parmsStr);
        } else {
            httpRequest.setPath(queryStr);
        }
        httpRequest.setParamMap(reqParms);
        return httpRequest;
    }

    public static Map<String, List<String>> parseParams(String parmsStr) {
        Map<String, List<String>> reqParms = new HashMap<>();
        String[] split1 = parmsStr.split("&");
        for (String param : split1) {
            String[] split3 = StrUtil.split(param, "=");
            List<String> strings = reqParms.get(split3[0]);
            if (strings == null) {
                strings = new ArrayList<>();
                reqParms.put(split3[0], strings);
            }
            strings.add(split3[1]);
        }
        return reqParms;
    }

}
