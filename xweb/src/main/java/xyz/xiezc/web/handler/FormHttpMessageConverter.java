package xyz.xiezc.web.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.web.common.ContentType;
import xyz.xiezc.web.common.MappingHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Component
public class FormHttpMessageConverter implements HttpMessageConverter {

    List<ContentType> contentTypes = new ArrayList<>() {{
        add(ContentType.FORM_URLENCODED);
    }};


    @Override
    public List<ContentType> getSupportContentType() {
        return contentTypes;
    }


    @Override
    public void read(MappingHandler mappingHandler, HttpServletRequest request) throws IOException {
        ParamDefinition[] paramDefinitions = mappingHandler.getParamDefinitions();
        Map<String, String[]> httpReqParams = ServletUtil.getParams(request);

        for (int i = 0; i < paramDefinitions.length; i++) {
            ParamDefinition paramDefinition = paramDefinitions[i];
            String name = paramDefinition.getName();
            String[] httpReqParam = httpReqParams.get(name);
            if (httpReqParam == null) {
                paramDefinition.setParam(null);
            }
            Class<?> type = paramDefinition.getParamType();

            if (ClassUtil.isAssignable(type, Set.class) || ClassUtil.isAssignable(type, List.class)) {
                Collection<Object> strings = CollUtil.toCollection(CollUtil.newArrayList(httpReqParam));
                paramDefinition.setParam(strings);
            }

            if (type.isArray()) {
                paramDefinition.setParam(httpReqParam); //paramObjs[i] = httpReqParam;
            }

            if (ClassUtil.isAssignable(type, Integer.class)) {
                Integer integer = Convert.toInt(httpReqParam[0]);
                paramDefinition.setParam(integer);

            }
            if (ClassUtil.isAssignable(type, String.class)) {
                paramDefinition.setParam(StrUtil.join(",", httpReqParam));
            }
            if (ClassUtil.isAssignable(type, Boolean.class)) {
                paramDefinition.setParam(Convert.toBool(httpReqParam[0]));
            }
            if (ClassUtil.isAssignable(type, Character.class)) {
                paramDefinition.setParam(Convert.toChar(StrUtil.join(",", httpReqParam)));
            }
            if (ClassUtil.isAssignable(type, Number.class)) {
                paramDefinition.setParam(Convert.toNumber(httpReqParam[0]));
            }
        }

    }

    @Override
    public void write(Object o, ContentType contentType, HttpServletResponse response) throws IOException {
        String build = ContentType.build(contentType.getValue(), CharsetUtil.CHARSET_UTF_8);
        response.setContentType(build);
        byte[] bytes = JSONUtil.toJsonStr(o).getBytes(CharsetUtil.CHARSET_UTF_8);
        response.getOutputStream().write(bytes);
    }
}
