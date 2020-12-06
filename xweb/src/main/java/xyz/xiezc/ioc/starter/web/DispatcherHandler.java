package xyz.xiezc.ioc.starter.web;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.file.PathUtil;
import cn.hutool.core.util.*;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.core.Init;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.common.Helper;
import xyz.xiezc.ioc.starter.web.converter.HttpMessageConverter;
import xyz.xiezc.ioc.starter.web.entity.RequestDefinition;
import xyz.xiezc.ioc.starter.web.entity.WebContext;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * 所有http请求的分发器
 *
 * @author xiezc
 */
@Component
public class DispatcherHandler {

    static Log log = LogFactory.get(DispatcherHandler.class);

    public static Map<String, RequestDefinition> requestDefinitionMap = new HashMap<>();

    private Map<ContentType, HttpMessageConverter> httpMessageConverterMap = new HashMap<>();

    @Autowire
    List<HttpMessageConverter> httpMessageConverters;

    @Init
    public void init() {
        for (HttpMessageConverter bean : httpMessageConverters) {
            List<ContentType> supportContentType = bean.getSupportContentType();
            supportContentType.forEach(contentType -> {
                httpMessageConverterMap.put(contentType, bean);
            });
        }
    }

    public FullHttpResponse doRequest(FullHttpRequest httpRequest) {
        try {
            WebContext.build(httpRequest);
            String uri = httpRequest.uri();
            HttpMethod httpMethod = httpRequest.method();
            log.info("url:{} method:{} ", uri, httpMethod);
            RequestDefinition requestDefinition = requestDefinitionMap.get(Helper.normalizeUrl(URLUtil.getPath(uri)));
            if (requestDefinition == null) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(StrUtil.bytes(HttpResponseStatus.NOT_FOUND.toString()));
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, byteBuf);

            }
            if (requestDefinition.getHttpMethod() != httpMethod) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(StrUtil.bytes(HttpResponseStatus.METHOD_NOT_ALLOWED.toString()));
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED, byteBuf);
            }

            HttpHeaders headers = httpRequest.headers();
            ContentType contentType = getContentType(headers, httpMethod);
            HttpMessageConverter httpMessageConverter = httpMessageConverterMap.get(contentType);
            if (httpMessageConverter == null) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(StrUtil.bytes(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE.toString()));
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE, byteBuf);
            }
            //获取请求的body
            LinkedHashMap<String, Parameter> parameterMap = requestDefinition.getParameterMap();
            Object[] paramaters = httpMessageConverter.parseParamaters(httpRequest, parameterMap);
            BeanDefinition beanDefinition = requestDefinition.getBeanDefinition();
            Object completedBean = beanDefinition.getCompletedBean();
            Object invoke = ReflectUtil.invoke(completedBean, requestDefinition.getInvokeMethod(), paramaters);
            //组装返回对象
            FullHttpResponse fullHttpResponse = buildFullHttpResponse(invoke, httpRequest);
            //处理返回的cookie
            Set<Cookie> cookies = WebContext.get().getCookies();
            if (CollUtil.isNotEmpty(cookies)) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Cookie respCookie : cookies) {
                    stringBuilder.append(respCookie.toString()).append(";");
                }
                fullHttpResponse.headers().set(SET_COOKIE, stringBuilder.toString());
            }
            return fullHttpResponse;
        } catch (Exception e) {
            log.error("服务器异常", e);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(StrUtil.bytes(e.getMessage()));
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, byteBuf);
        }
    }


    private ContentType getContentType(HttpHeaders headers, HttpMethod httpMethod) {
        if (StrUtil.equalsIgnoreCase(httpMethod.name(), "get")) {
            return ContentType.Default;
        }
        String contentTypeStr = headers.get("Content-Type");
        if (StrUtil.isBlank(contentTypeStr)) {
            contentTypeStr = headers.get("content-type");
        }
        if (contentTypeStr == null) {
            return null;
        }
        ContentType contentType;
        if (StrUtil.equalsIgnoreCase(httpMethod.name(), "post")) {
            contentType = ContentType.getByValue(contentTypeStr);
        } else {
            contentType = ContentType.Default;
        }
        return contentType;
    }


    private FullHttpResponse buildFullHttpResponse(Object result, FullHttpRequest httpRequest) throws UnsupportedEncodingException {
        if (result == null) {
            result = "";
        }

        String respStr = "";
        if (ClassUtil.isSimpleValueType(result.getClass())) {
            respStr = Convert.toStr(result);
        } else {
            respStr = JSONUtil.toJsonStr(result);
        }

        HttpVersion httpVersion = httpRequest.protocolVersion();
        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion, OK,
                Unpooled.wrappedBuffer(StrUtil.bytes(respStr, CharsetUtil.UTF_8)));
        response.headers()
                .set(CONTENT_TYPE, ContentType.JSON.getValue())
                .setInt(CONTENT_LENGTH, response.content().readableBytes());

        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
        if (keepAlive) {
            if (!httpVersion.isKeepAliveDefault()) {
                response.headers().set(CONNECTION, keepAlive);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }
        return response;
    }

}
