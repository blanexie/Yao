package xyz.xiezc.ioc.starter.web.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.annotation.RequestBody;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.entity.HttpResponse;
import xyz.xiezc.ioc.starter.web.enums.HttpResponseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public HttpResponse read(MethodDefinition methodDefinition, ContentType contentType, HttpRequest request) throws IOException {
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
            ParamDefinition paramDefinition = requestBodyParam;
            List<String> body = new ArrayList<>(

            );
            //TODO
            String bodyStr = body.get(0);
            //数组的
            if (JSONUtil.isJsonArray(bodyStr)) {
                JSONArray objects = JSONUtil.parseArray(bodyStr);
                Class paramType = paramDefinition.getParamType();
                if (ClassUtil.isAssignable(paramType, List.class) || ClassUtil.isAssignable(paramType, Set.class)) {
                    paramDefinition.setParam(CollUtil.toCollection(objects));
                }
                if (paramType.isArray()) {
                    paramDefinition.setParam(objects.toArray());
                }
            }
            if (JSONUtil.isJsonObj(bodyStr)) {
                Object o = JSONUtil.toBean(bodyStr, paramDefinition.getParamType());
                HttpResponse httpResponse = DispatcherHandler.invokeMappingMethod(methodDefinition, o);
                return httpResponse;
            }
            return DispatcherHandler.failHeader(HttpResponseStatus.BAD_REQUEST, "无法正确解析");
        }
        throw new RuntimeException(methodDefinition.getMethod().getName() + "需要一个@RequestBody注解 ");
    }

}
