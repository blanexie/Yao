package xyz.xiezc.ioc.starter.starter.web.converter;

import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.core.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.starter.web.entity.HttpRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * multipart/form-data 类型的请求解析
 */
@Component
public class MultipartFormHttpMessageConverter implements HttpMessageConverter {
    List<ContentType> contentTypes = new ArrayList<>() {{
        add(ContentType.MULTIPART);
    }};

    @Override
    public List<ContentType> getSupportContentType() {
        return contentTypes;
    }

    @Override
    public Object[] doRead(MethodDefinition methodDefinition, ContentType contentType, HttpRequest request) {
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        Map<String, List<String>> paramMap = request.getBodyParamMap();
        Object[] objects = parseFormData(request.getFileItems(), paramDefinitions,paramMap);
        return objects;
    }

}
