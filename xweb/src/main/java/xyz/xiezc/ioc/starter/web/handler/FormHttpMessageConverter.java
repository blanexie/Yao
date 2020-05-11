package xyz.xiezc.ioc.starter.web.handler;

import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.entity.FileItem;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

import java.util.ArrayList;
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
        Object[] objects = parseFormData(request, paramDefinitions);
        return objects;
    }

    private Object[] parseFormData(HttpRequest request, ParamDefinition[] paramDefinitions) {
        Object[] res = new Object[paramDefinitions.length];
        Map<String, List<String>> paramMap = request.getBodyParamMap();
        for (int i = 0; i < paramDefinitions.length; i++) {
            ParamDefinition paramDefinition = paramDefinitions[i];
            String name = paramDefinition.getParamName();
            if (ClassUtil.isAssignable(FileItem.class, paramDefinition.getParamType())) {
                res[i] = request.getFileItems().get(name);
                continue;
            }
            List<String> collect = paramMap.get(name);
            if (collect == null) {
                res[i] = null;
                continue;
            }
            res[i] = paramMapping(collect, paramDefinition);
        }
        return res;
    }

}
