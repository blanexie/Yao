package xyz.xiezc.ioc.starter.web;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Init;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.converter.HttpMessageConverter;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.entity.RequestDefinition;
import xyz.xiezc.ioc.starter.web.entity.WebContext;
import xyz.xiezc.ioc.starter.web.netty.websocket.WebSocketFrameHandler;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Parameter;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * 所有http请求的分发器
 */
@Component
public class DispatcherHandler {

    static Log log = LogFactory.get(DispatcherHandler.class);

    public static Map<String, RequestDefinition> requestDefinitionMap = new HashMap<>();


    public static Map<String, WebSocketFrameHandler> webSocketFrameHandlerMap = new HashMap<>();
    public static Map<ContentType, HttpMessageConverter> httpMessageConverterMap = new HashMap<>();

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


    private FullHttpResponse doRequest(io.netty.handler.codec.http.FullHttpRequest httpRequest) {
        try {
            WebContext.build(httpRequest);
            String uri = httpRequest.uri();
            HttpMethod httpMethod = httpRequest.method();
            log.info("url:{} method:{} ");

            RequestDefinition requestDefinition = requestDefinitionMap.get(uri);
            if (requestDefinition.getHttpMethod() != httpMethod) {
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
            }

            HttpHeaders headers = httpRequest.headers();
            ContentType contentType = getContentType(headers, httpMethod);
            HttpMessageConverter httpMessageConverter = httpMessageConverterMap.get(contentType);
            if (httpMessageConverter == null) {
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
            }

            //获取请求的body
            ByteBuf content = httpRequest.content();
            byte[] bytes = ByteBufUtil.getBytes(content);
            LinkedHashMap<String, Parameter> parameterMap = requestDefinition.getParameterMap();
            Object[] paramaters = httpMessageConverter.parseParamaters(bytes, parameterMap);
            BeanDefinition beanDefinition = requestDefinition.getBeanDefinition();
            Object completedBean = beanDefinition.getCompletedBean();
            Object invoke = ReflectUtil.invoke(completedBean, requestDefinition.getInvokeMethod(), paramaters);
            //组装返回对象
            FullHttpResponse fullHttpResponse = buildFullHttpResponse(invoke, httpRequest);
            //处理返回的cookie
            Set<Cookie> cookies = WebContext.get().getCookies();
            StringBuilder stringBuilder = new StringBuilder();
            for (Cookie respCookie : cookies) {
                stringBuilder.append(respCookie.toString()).append(";");
            }
            fullHttpResponse.headers().set(SET_COOKIE, stringBuilder.toString());
            return fullHttpResponse;
        } catch (Exception e) {
            String result = ExceptionUtil.stacktraceToString(e, 10);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(StrUtil.bytes(result));
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, byteBuf);
        }
    }


    private ContentType getContentType(HttpHeaders headers, HttpMethod httpMethod) {
        String contentTypeStr = headers.get("Content-Type");
        if (StrUtil.isBlank(contentTypeStr)) {
            contentTypeStr = headers.get("content-type");
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
        HttpVersion httpVersion = httpRequest.protocolVersion();

        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion, OK,
                Unpooled.wrappedBuffer(JSONUtil.toJsonStr(result).getBytes(CharsetUtil.UTF_8)));
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
