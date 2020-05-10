package xyz.xiezc.ioc.starter.web;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Init;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.handler.HttpMessageConverter;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@Component
public class DispatcherHandler {

    static Log log = LogFactory.get(DispatcherHandler.class);

    public static Map<String, MethodDefinition> getMethods = new HashMap<>();
    public static Map<String, MethodDefinition> postMethods = new HashMap<>();

    public static Map<ContentType, HttpMessageConverter> httpMessageConverterMap = new HashMap<>();


    @Init
    public void init() {
        List<BeanDefinition> beanDefinitions = Xioc.getApplicationContext().getBeanDefinitions(HttpMessageConverter.class);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            HttpMessageConverter bean = beanDefinition.getBean();
            List<ContentType> supportContentType = bean.getSupportContentType();
            supportContentType.forEach(contentType -> {
                httpMessageConverterMap.put(contentType, bean);
            });
        }
    }


    private MethodDefinition getMethodDefinition(HttpRequest httpRequest) {
        String method = httpRequest.getMethod();
        MethodDefinition methodDefinition = null;
        if (StrUtil.equalsIgnoreCase(method, "get")) {
            methodDefinition = getMethods.get(httpRequest.getPath());
        } else if (StrUtil.equalsIgnoreCase(method, "post")) {
            methodDefinition = postMethods.get(httpRequest.getPath());
        } else {
            String content = "{\"msg\":\"目前只支持GET和POST请求\"}";
            throw new RuntimeException(content);
        }
        if (methodDefinition == null) {
            String content = "{\"msg\":\"" + httpRequest.getPath() + " 路径没找到\"}";
            throw new RuntimeException(content);
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

    public FullHttpResponse doRequest(HttpRequest httpRequest) {
        try {
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
            return response;
        } catch (Exception e) {
            log.error(e);
            FullHttpRequest fullHttpRequest = httpRequest.getFullHttpRequest();
            FullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), OK,
                    Unpooled.wrappedBuffer(JSONUtil.toJsonStr(e.getMessage()).getBytes(CharsetUtil.CHARSET_UTF_8)));
            return response;
        }
    }


    private FullHttpResponse getFullHttpResponse(Object result, HttpRequest httpRequest) throws UnsupportedEncodingException {
        FullHttpRequest fullHttpRequest = httpRequest.getFullHttpRequest();
        FullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), OK,
                Unpooled.wrappedBuffer(JSONUtil.toJsonStr(result).getBytes(CharsetUtil.UTF_8)));
        response.headers()
                .set(CONTENT_TYPE, ContentType.JSON.getValue())
                .setInt(CONTENT_LENGTH, response.content().readableBytes());

        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);

        if (keepAlive) {
            if (!fullHttpRequest.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, keepAlive);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }
        return response;
    }

}
