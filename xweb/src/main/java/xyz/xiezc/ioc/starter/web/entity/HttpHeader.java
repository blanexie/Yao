package xyz.xiezc.ioc.starter.web.entity;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class HttpHeader {
    String key;

    String value;

    public HttpHeader(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public HttpHeader(String paramStr) {
        String[] split = StrUtil.split(paramStr, "=");
        this.key = split[0];
        this.value = split[1];
    }

}
