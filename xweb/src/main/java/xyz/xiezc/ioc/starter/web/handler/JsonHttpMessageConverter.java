package xyz.xiezc.ioc.starter.web.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.web.annotation.RequestBody;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.entity.HttpContent;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JsonHttpMessageConverter implements HttpMessageConverter<Object> {


    List<ContentType> contentTypes = new ArrayList<>() {{
        add(ContentType.JSON);
    }};


    @Override
    public List<ContentType> getSupportContentType() {
        return contentTypes;
    }


    @Override
    public Object[] doRead(MethodDefinition methodDefinition, ContentType contentType, HttpRequest request) throws IOException {
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        ParamDefinition requestBodyParam = null;
        for (ParamDefinition paramDefinition : paramDefinitions) {
            RequestBody annotation = AnnotationUtil.getAnnotation(paramDefinition.getAnnotatedElement(), RequestBody.class);
            if (annotation == null) {
                continue;
            }
            requestBodyParam = paramDefinition;
            break;
        }

        //有一个RequestBody参数的配置
        if (requestBodyParam != null) {
            Object param = null;
            ParamDefinition paramDefinition = requestBodyParam;
            List<HttpContent> bodyList = request.getBodyList();
            String bodyStr = bodyList.get(0).getValue();
            Class paramType = paramDefinition.getParamType();
            if (ClassUtil.isSimpleValueType(paramType)) {
                param = JSONUtil.toBean(bodyStr, paramType);
            }
            if (paramType.isArray()) {
                JSONArray objects = JSONUtil.parseArray(bodyStr);
                param = objects.toArray();
            }
            if (ClassUtil.isAssignable(Collection.class, paramType)) {
                JSONArray objects = JSONUtil.parseArray(bodyStr);
                param = CollUtil.toCollection(objects);
            }
            return new Object[]{param};
        }
        throw new RuntimeException(methodDefinition.getMethod().getName() + "需要一个@RequestBody注解 ");
    }

}
