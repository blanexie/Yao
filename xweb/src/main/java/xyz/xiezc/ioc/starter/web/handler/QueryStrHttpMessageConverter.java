package xyz.xiezc.ioc.starter.web.handler;

import cn.hutool.core.collection.CollUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class QueryStrHttpMessageConverter implements HttpMessageConverter {

    @Override
    public List<ContentType> getSupportContentType() {
        return CollUtil.newArrayList(ContentType.Default);
    }



    @Override
    public Object[] doRead(MethodDefinition methodDefinition, ContentType contentType, HttpRequest request)  {
        Map<String, List<String>> paramMap = request.getQueryParamMap();
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
