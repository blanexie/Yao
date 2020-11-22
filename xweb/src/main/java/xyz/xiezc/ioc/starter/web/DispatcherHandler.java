package xyz.xiezc.ioc.starter.web;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Init;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.common.XWebException;
import xyz.xiezc.ioc.starter.web.converter.HttpMessageConverter;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.entity.RequestDefinition;
import xyz.xiezc.ioc.starter.web.entity.WebContext;
import xyz.xiezc.ioc.starter.web.netty.websocket.WebSocketFrameHandler;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    private FullHttpResponse getMethodDefinition(io.netty.handler.codec.http.FullHttpRequest httpRequest) {
        String uri = httpRequest.uri();
        HttpMethod httpMethod = httpRequest.method();

        RequestDefinition requestDefinition = requestDefinitionMap.get(uri);
        if (requestDefinition.getHttpMethod() != httpMethod) {
            throw new RuntimeException("没找到对应url的controller方法, path:" + uri);
        }

        HttpHeaders headers = httpRequest.headers();
        String contentType = headers.get("Content-Type");

        HttpMessageConverter httpMessageConverter = httpMessageConverterMap.get(contentType);
        if(httpMessageConverter==null){
            HttpVersion version;
            HttpResponseStatus status;
            FullHttpResponse fullHttpResponse=
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
            fullHttpResponse.
            return
        }


        ByteBuf content = httpRequest.content();

        byte[] bytes = ByteBufUtil.getBytes(content);
        String body=new String(bytes);




        String method = httpRequest.getMethod();
        MethodDefinition methodDefinition = null;
        if (StrUtil.equalsIgnoreCase(method, "get")) {
            methodDefinition = getMethods.get(httpRequest.getPath());
        } else if (StrUtil.equalsIgnoreCase(method, "post")) {
            methodDefinition = postMethods.get(httpRequest.getPath());
        } else {
            throw new XWebException(HttpResponseStatus.NOT_FOUND.code(), "目前只支持GET和POST请求");
        }
        if (methodDefinition == null) {
            throw new XWebException(HttpResponseStatus.NOT_FOUND.code(), "路径没找到");
        }
        return methodDefinition;
    }


    public ContentType getContentType(HttpRequest httpRequest) {
        String method = httpRequest.getMethod();
        ContentType contentType;
        if (StrUtil.equalsIgnoreCase(method, "post")) {
            String contentTypeStr = httpRequest.getContentType();
            contentType = ContentType.getByValue(contentTypeStr);
        } else {
            contentType = ContentType.Default;
        }
        return contentType;
    }

    public Object getResult(MethodDefinition methodDefinition, Object[] params) {
        BeanDefinition beanDefinition = methodDefinition.getBeanDefinition();
        Object bean = beanDefinition.getBean();
        return ReflectUtil.invoke(bean, methodDefinition.getMethod(), params);
    }

    @SneakyThrows
    public FullHttpResponse doRequest(HttpRequest httpRequest) {
        WebContext build = WebContext.build(httpRequest);

        //1. 找到对应的方法处理类
        MethodDefinition methodDefinition = this.getMethodDefinition(httpRequest);
        //2. 获取参数转换器
        ContentType contentType = getContentType(httpRequest);
        HttpMessageConverter httpMessageConverter = httpMessageConverterMap.get(contentType);
        //3. 转换请求参数,
        Object[] objects = httpMessageConverter.doRead(methodDefinition, contentType, httpRequest);
        //4. 反射调用方法
        Object result = getResult(methodDefinition, objects);
        //5. 组装响应结果
        FullHttpResponse response = getFullHttpResponse(result, httpRequest);
        StringBuilder stringBuilder = new StringBuilder();
        for (Cookie respCookie : build.getRespCookies()) {
            stringBuilder.append(respCookie.toString()).append(";");
        }
        response.headers().set(SET_COOKIE, stringBuilder.toString());
        return response;
    }


    private FullHttpResponse getFullHttpResponse(Object result, HttpRequest httpRequest) throws UnsupportedEncodingException {
        if (result == null) {
            result = "";
        }
        FullHttpResponse response = new DefaultFullHttpResponse(httpRequest.getHttpVersion(), OK,
                Unpooled.wrappedBuffer(JSONUtil.toJsonStr(result).getBytes(CharsetUtil.UTF_8)));
        response.headers()
                .set(CONTENT_TYPE, ContentType.JSON.getValue())
                .setInt(CONTENT_LENGTH, response.content().readableBytes());

        boolean keepAlive = httpRequest.isKeepAlive();
        if (keepAlive) {
            if (!httpRequest.getHttpVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, keepAlive);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }
        return response;
    }

}
