package xyz.xiezc.ioc.starter.web.entity;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.IdUtil;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class WebContext {

    static FastThreadLocal<WebContext> fastThreadLocal = new FastThreadLocal<>();

    public static TimedCache<String, Map<String, Object>> sessionCache = new TimedCache<>(30 * 60 * 1000);

    public static WebContext get() {
        WebContext webContext = fastThreadLocal.get();
        if (webContext == null) {
            webContext = new WebContext();
            fastThreadLocal.set(webContext);
        }
        return webContext;
    }

    public static WebContext build(HttpRequest httpRequest) {
        WebContext webContext = fastThreadLocal.get();
        if (webContext == null) {
            webContext = new WebContext();
            fastThreadLocal.set(webContext);
        }
        webContext.setHttpRequest(httpRequest);
        return webContext;
    }

    public Map<String, Object> getSession() {
        Set<Cookie> cookies = httpRequest.getCookies();
        for (Cookie cookie : cookies) {
            String name = cookie.name();
            String value = cookie.value();
            if (Objects.equals("session_id", name)) {
                return sessionCache.get(cookie.domain() + value);
            }
        }
        String host = httpRequest.getNettyHttpRequest().headers().get("host");
        Cookie cookie = new DefaultCookie("session_id", IdUtil.fastSimpleUUID());
        cookie.setDomain(host);
        cookie.setHttpOnly(true);
        respCookies.add(cookie);
        Map<String, Object> objectObjectHashMap = new HashMap<>();

        sessionCache.put(cookie.domain() + cookie.value(), objectObjectHashMap);

        return objectObjectHashMap;
    }


    HttpRequest httpRequest;

    Set<Cookie> respCookies = new HashSet<>();
}
