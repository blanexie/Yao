package xyz.xiezc.ioc.starter.starter.web.converter;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import io.netty.buffer.ByteBuf;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.starter.web.annotation.RequestBody;
import xyz.xiezc.ioc.starter.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.starter.web.common.XWebException;
import xyz.xiezc.ioc.starter.starter.web.entity.HttpRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class JsonHttpMessageConverter implements HttpMessageConverter {

    List<ContentType> contentTypes = new ArrayList<>() {{
        add(ContentType.JSON);
    }};

    @Override
    public List<ContentType> getSupportContentType() {
        return contentTypes;
    }

    @Override
    public Object[] doRead(MethodDefinition methodDefinition, ContentType contentType, HttpRequest request) {
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();

        //参数只有一个， 则必须有RequestBody注解。
        if (paramDefinitions.length == 1) {
            ParamDefinition paramDefinition = paramDefinitions[0];
            return getInvokeParams(request, paramDefinition);
        }
        //如果存在多个参数， 找到有RequestBody注解的参数
        Optional<ParamDefinition> first = Stream.of(paramDefinitions)
                .filter(paramDefinition -> {
                    RequestBody annotation = AnnotationUtil.getAnnotation(paramDefinition.getAnnotatedElement(), RequestBody.class);
                    return annotation != null;
                }).findFirst();
        if (first.isPresent()) {
            return getInvokeParams(request, first.get());
        }
        //走到这里就是注解没有配置
        throw new XWebException(methodDefinition.getMethod().getName() + "需要一个@RequestBody注解 ");
    }

    private Object[] getInvokeParams(HttpRequest request, ParamDefinition paramDefinition) {
        RequestBody requestBody = AnnotationUtil.getAnnotation(paramDefinition.getAnnotatedElement(), RequestBody.class);
        if (requestBody == null) {
            throw new XWebException("Application/json类型的请求需要配置@RequestBody注解");
        }
        ByteBuf body = request.getBody();
        CharSequence charSequence = body.readCharSequence(body.readableBytes(), CharsetUtil.CHARSET_UTF_8);
        String bodyStr = charSequence.toString();
        Class paramType = paramDefinition.getParamType();
        Object param = null;
        if (ClassUtil.isSimpleValueType(paramType)) {

        } else if (paramType.isArray()) {
            JSONArray objects = JSONUtil.parseArray(bodyStr);
            param = objects.toArray();
        } else if (ClassUtil.isAssignable(Collection.class, paramType)) {
            JSONArray objects = JSONUtil.parseArray(bodyStr);
            param = CollUtil.toCollection(objects);
        } else {
            //其他类型
            param = JSONUtil.toBean(bodyStr, paramType);
        }
        return new Object[]{param};
    }

}
