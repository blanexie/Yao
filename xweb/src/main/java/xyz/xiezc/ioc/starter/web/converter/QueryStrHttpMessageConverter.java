package xyz.xiezc.ioc.starter.web.converter;

import cn.hutool.core.collection.CollUtil;
import xyz.xiezc.ioc.annotation.core.Component;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

import java.util.List;
import java.util.Map;

@Component
public class QueryStrHttpMessageConverter implements HttpMessageConverter {

    @Override
    public List<ContentType> getSupportContentType() {
        return CollUtil.newArrayList(ContentType.Default);
    }


    @Override
    public Object[] doRead(MethodDefinition methodDefinition, ContentType contentType, HttpRequest request) {
        Map<String, List<String>> paramMap = request.getQueryParamMap();
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        return this.parseFormData(null, paramDefinitions, paramMap);
    }


}
