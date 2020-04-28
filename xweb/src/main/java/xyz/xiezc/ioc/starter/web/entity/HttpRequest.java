package xyz.xiezc.ioc.starter.web.entity;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class HttpRequest {

    String path;

    String method;

    List<HttpHeader> headers=new ArrayList<>();

    Map<String,List<String>> paramMap=new HashMap<>();

    public static void build(String queryStr) {
        HttpRequest httpRequest = new HttpRequest();
        Map<String, List<String>> reqParms = new HashMap<>();
        if (queryStr.contains("?")) {
            String[] split = StrUtil.split(queryStr, "?");
            httpRequest.setPath(split[0]);
            String parmsStr = split[1];
            String[] split1 = parmsStr.split("&");
            for (String param : split1) {
                String[] split3 = StrUtil.split(paramStr, "=");
                List<String> strings = paramMap.get(split3[0]);
                HttpHeader httpHeader = new HttpHeader(param);

            }
        }

    }

}
