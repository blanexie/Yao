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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FormHttpMessageConverter implements HttpMessageConverter {

    List<ContentType> contentTypes = new ArrayList<>() {{
        add(ContentType.FORM_URLENCODED);
        add(ContentType.MULTIPART);
    }};


    @Override
    public List<ContentType> getSupportContentType() {
        return contentTypes;
    }


    @Override
    public Object[] doRead(MethodDefinition methodDefinition, ContentType contentType, HttpRequest request) {
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        if (contentType == ContentType.FORM_URLENCODED) {
            Object[] objects = parseFormUrlencoded(request, paramDefinitions);
            return objects;
        }
        if (contentType == ContentType.MULTIPART) {
            Object[] objects = parseFormData(request, paramDefinitions);
            return objects;
        }
        return null;
    }


    private Map<String, List<String>> getRequestParams(FullHttpRequest request) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(true), request);
        List<InterfaceHttpData> httpPostData = decoder.getBodyHttpDatas();
        Map<String, List<String>> params = new HashMap<>();
        for (InterfaceHttpData data : httpPostData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                List<String> orDefault = params.getOrDefault(attribute.getName(), CollUtil.newArrayList());
                orDefault.add(attribute.getValue());
                params.put(attribute.getName(), orDefault);
            }
        }
        return params;
    }

    private Object[] parseFormData(HttpRequest request, ParamDefinition[] paramDefinitions) {
        Object[] res = new Object[paramDefinitions.length];
        FullHttpRequest fullHttpRequest = request.getFullHttpRequest();
        Map<String, List<String>> paramMap = this.getRequestParams(fullHttpRequest);

        for (int i = 0; i < paramDefinitions.length; i++) {
            ParamDefinition paramDefinition = paramDefinitions[i];
            String name = paramDefinition.getParamName();
            List<String> collect = paramMap.get(name);
            if (collect == null) {
                res[i] = null;
                continue;
            }
            Object o1 = paramMapping(collect, paramDefinition);
            res[i] = o1;
        }
        return res;
    }

    private Object[] parseFormUrlencoded(HttpRequest request, ParamDefinition[] paramDefinitions) {
        Object[] res = new Object[paramDefinitions.length];
        FullHttpRequest fullHttpRequest = request.getFullHttpRequest();
        Map<String, List<String>> paramMap = this.getRequestParams(fullHttpRequest);
        for (int i = 0; i < paramDefinitions.length; i++) {
            ParamDefinition paramDefinition = paramDefinitions[i];
            String name = paramDefinition.getParamName();
            Object o1 = paramMapping(paramMap.get(name), paramDefinition);
            res[i] = o1;
        }
        return res;
    }


}
