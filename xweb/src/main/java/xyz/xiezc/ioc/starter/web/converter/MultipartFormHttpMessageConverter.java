package xyz.xiezc.ioc.starter.web.converter;

import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

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
