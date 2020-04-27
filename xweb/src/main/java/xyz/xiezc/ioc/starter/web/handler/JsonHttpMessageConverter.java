//package xyz.xiezc.ioc.starter.web.handler;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.exceptions.ExceptionUtil;
//import cn.hutool.core.util.CharsetUtil;
//import cn.hutool.core.util.ClassUtil;
//import cn.hutool.json.JSONArray;
//import cn.hutool.json.JSONUtil;
//import xyz.xiezc.ioc.annotation.Component;
//import xyz.xiezc.ioc.definition.ParamDefinition;
//import xyz.xiezc.ioc.starter.web.annotation.RequestBody;
//import xyz.xiezc.ioc.starter.web.common.ContentType;
//import xyz.xiezc.ioc.starter.web.common.MappingHandler;
//
//import javax.servlet.ServletInputStream;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.lang.annotation.Annotation;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Component
//public class JsonHttpMessageConverter implements HttpMessageConverter<Object> {
//
//
//    List<ContentType> contentTypes = new ArrayList<>() {{
//        add(ContentType.JSON);
//    }};
//
//
//    @Override
//    public List<ContentType> getSupportContentType() {
//        return contentTypes;
//    }
//
//
//    @Override
//    public void read(MappingHandler mappingHandler,ContentType contentType, HttpServletRequest request) throws IOException {
//        ParamDefinition[] paramDefinitions = mappingHandler.getParamDefinitions();
//        List<ParamDefinition> collect = CollUtil.newArrayList(paramDefinitions)
//                .stream()
//                .filter(paramDefinition -> {
//                    List<Annotation> annotations = paramDefinition.getAnnotations();
//                    if (annotations == null) {
//                        return false;
//                    }
//                    long count = annotations.stream()
//                            .filter(annotation ->
//                                    annotation.annotationType() == RequestBody.class
//                            ).count();
//                    return count > 0;
//                }).collect(Collectors.toList());
//
//        //有一个RequestBody参数的配置
//        if (collect.size() == 1) {
//            ParamDefinition paramDefinition = collect.get(0);
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream(), CharsetUtil.CHARSET_UTF_8));
//            StringBuilder stringBuilder = new StringBuilder();
//            bufferedReader.lines()
//                    .forEach(line -> stringBuilder.append(line));
//            String bodyStr = stringBuilder.toString();
//            //数组的
//            if (JSONUtil.isJsonArray(bodyStr)) {
//                JSONArray objects = JSONUtil.parseArray(bodyStr);
//                Class paramType = paramDefinition.getParamType();
//                if (ClassUtil.isAssignable(paramType, List.class) || ClassUtil.isAssignable(paramType, Set.class)) {
//                    paramDefinition.setParam(CollUtil.toCollection(objects));
//                }
//                if (paramType.isArray()) {
//                    paramDefinition.setParam(objects.toArray());
//                }
//            }
//            if (JSONUtil.isJsonObj(bodyStr)) {
//                paramDefinition.setParam(JSONUtil.toBean(stringBuilder.toString(), paramDefinition.getParamType()));
//            }
//            return;
//        }
//
//        ExceptionUtil.wrapAndThrow(
//                new RuntimeException(mappingHandler.getMethod() + "需要一个@RequestBody注解 ")
//        );
//
//    }
//
//    @Override
//    public void write(Object o, ContentType contentType, HttpServletResponse response) throws IOException {
//        response.setStatus(HttpServletResponse.SC_OK);
//        String build = ContentType.build(contentType.getValue(), CharsetUtil.CHARSET_UTF_8);
//        response.setContentType(build);
//        byte[] bytes = JSONUtil.toJsonStr(o).getBytes(CharsetUtil.CHARSET_UTF_8);
//        response.getOutputStream().write(bytes);
//    }
//
//}
