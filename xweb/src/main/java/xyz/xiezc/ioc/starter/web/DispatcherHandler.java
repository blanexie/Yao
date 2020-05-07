package xyz.xiezc.ioc.starter.web;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.handler.codec.http2.Http2Headers;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.common.WebContext;
import xyz.xiezc.ioc.starter.web.entity.HttpHeader;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.entity.HttpResponse;
import xyz.xiezc.ioc.starter.web.enums.HttpResponseStatus;
import xyz.xiezc.ioc.starter.web.handler.HttpMessageConverter;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.xiezc.ioc.starter.web.enums.HttpResponseStatus.BAD_REQUEST;

@Component
public class DispatcherHandler {

    static Log log = LogFactory.get(DispatcherHandler.class);


    public static Cache<Integer, Http2Headers> headerCache = CacheUtil.newTimedCache(30000);
    public static Cache<Integer, HttpRequest> requestCache = CacheUtil.newTimedCache(30000);
    public static Map<String, MethodDefinition> getMethods = new HashMap<>();
    public static Map<String, MethodDefinition> postMethods = new HashMap<>();
    public static Map<ContentType, HttpMessageConverter> httpMessageConverterMap = new HashMap<>();


    public static HttpResponse doRequest(HttpRequest httpRequest) {
        try {
            WebContext.get().setRequest(httpRequest);
            //找到对应的类
            String method = httpRequest.getMethod();
            MethodDefinition methodDefinition = null;
            if (StrUtil.equalsIgnoreCase(method, "get")) {
                methodDefinition = getMethods.get(httpRequest.getPath());
            } else if (StrUtil.equalsIgnoreCase(method, "post")) {
                methodDefinition = postMethods.get(httpRequest.getPath());
            } else {
                String content = "{\"msg\":\"目前只支持GET和POST请求\"}";
                HttpResponse httpResponse = failHeader(HttpResponseStatus.METHOD_NOT_ALLOWED, content);
                return httpResponse;
            }
            if (methodDefinition == null) {
                String content = "{\"msg\":\"" + httpRequest.getPath() + " 路径没找到\"}";
                HttpResponse httpResponse = failHeader(HttpResponseStatus.NOT_FOUND, content);
                return httpResponse;
            }
            if (StrUtil.equalsIgnoreCase(method, "post")) {
                Optional<HttpHeader> first = httpRequest.getHeaders().stream()
                        .filter(httpHeader -> {
                            String contentStr = httpHeader.getKey().replaceAll("-", "");
                            return StrUtil.equalsIgnoreCase(contentStr, "contenttype");
                        }).findFirst();
                HttpHeader httpHeader = first.get();
                String value = httpHeader.getValue();
                ContentType byValue = ContentType.getByValue(value);
                HttpMessageConverter httpMessageConverter = httpMessageConverterMap.get(byValue);
                return httpMessageConverter.read(methodDefinition, byValue, httpRequest);
            }

            if (StrUtil.equalsIgnoreCase(method, "get")) {
                HttpMessageConverter httpMessageConverter = httpMessageConverterMap.get(ContentType.Default);
                return httpMessageConverter.read(methodDefinition, null, httpRequest);
            }
            return DispatcherHandler.failHeader(BAD_REQUEST, "未找到对应类型的contentType的解析方式");
        } catch (Exception e) {
            log.error(e);
            return DispatcherHandler.failHeader(BAD_REQUEST, e.getMessage());
        }
    }

    public static HttpResponse failHeader(HttpResponseStatus httpResponseStatus, String content) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setHttpResponseStatus(httpResponseStatus);
        httpResponse.setBody(content);
        HttpHeader httpHeader = new HttpHeader("content-type", "application/json;charset=utf-8");
        httpResponse.setHeaders(CollUtil.newArrayList(httpHeader));
        return httpResponse;
    }

    public static HttpResponse invokeMappingMethod(MethodDefinition methodDefinition, Object... paramObjs) {
        BeanDefinition beanDefinition = methodDefinition.getBeanDefinition();
        Object bean = beanDefinition.getBean();
        Object invoke = ReflectUtil.invoke(bean, methodDefinition.getMethod(), paramObjs);
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setBody(invoke);
        HttpHeader httpHeader = new HttpHeader("content-type", "application/json;charset=utf-8");
        httpResponse.setHeaders(CollUtil.newArrayList(httpHeader));
        httpResponse.setHttpResponseStatus(HttpResponseStatus.OK);
        return httpResponse;
    }


}
