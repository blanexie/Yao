package xyz.xiezc.ioc.starter.web.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.entity.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class QueryStrHttpMessageConverter implements HttpMessageConverter {

    @Override
    public List<ContentType> getSupportContentType() {
        return CollUtil.newArrayList(ContentType.Default);
    }

    @Override
    public Object[] doRead(MethodDefinition methodDefinition, ContentType contentType, HttpRequest request) throws IOException {
        Map<String, List<String>> paramMap = request.getParamMap();
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        Object[] paramObjs = new Object[paramDefinitions.length];
        for (int i = 0; i < paramDefinitions.length; i++) {
            ParamDefinition paramDefinition = paramDefinitions[i];
            String paramName = paramDefinition.getParamName();
            List<String> params = paramMap.get(paramName);
            if (params == null || params.isEmpty()) {
                paramObjs[i] = null;
                continue;
            }
            paramObjs[i] = paramMapping(params, paramDefinition);
        }
        return paramObjs;
    }


}
