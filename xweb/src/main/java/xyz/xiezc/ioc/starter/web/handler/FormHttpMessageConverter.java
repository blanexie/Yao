//package xyz.xiezc.ioc.starter.web.handler;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.bean.copier.CopyOptions;
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.convert.Convert;
//import cn.hutool.core.net.multipart.MultipartFormData;
//import cn.hutool.core.net.multipart.UploadFile;
//import cn.hutool.core.util.CharsetUtil;
//import cn.hutool.core.util.ClassUtil;
//import cn.hutool.core.util.ReflectUtil;
//import cn.hutool.extra.servlet.ServletUtil;
//import cn.hutool.json.JSONUtil;
//import xyz.xiezc.ioc.annotation.Component;
//import xyz.xiezc.ioc.definition.ParamDefinition;
//import xyz.xiezc.ioc.starter.web.common.ContentType;
//import xyz.xiezc.ioc.starter.web.common.MappingHandler;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.lang.annotation.Annotation;
//import java.util.*;
//
//@Component
//public class FormHttpMessageConverter implements HttpMessageConverter {
//
//    List<ContentType> contentTypes = new ArrayList<>() {{
//        add(ContentType.FORM_URLENCODED);
//        add(ContentType.MULTIPART);
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
//    public void read(MappingHandler mappingHandler, ContentType contentType, HttpServletRequest request) throws IOException {
//        ParamDefinition[] paramDefinitions = mappingHandler.getParamDefinitions();
//
//        if (contentType == ContentType.FORM_URLENCODED) {
//            parseFormUrlencode(request, paramDefinitions);
//        }
//        if (contentType == ContentType.MULTIPART) {
//            parseFormData(request, paramDefinitions);
//        }
//    }
//
//    private void parseFormData(HttpServletRequest request, ParamDefinition[] paramDefinitions) {
//        MultipartFormData multipart = ServletUtil.getMultipart(request);
//        if (paramDefinitions.length == 1) {
//            ParamDefinition paramDefinition = paramDefinitions[0];
//            Class paramType = paramDefinition.getParamType();
//            if (!ClassUtil.isBasicType(paramType) && paramType != String.class) {
//                Object o = ReflectUtil.newInstance(paramType);
//                Map<String, Object> map = new HashMap<>();
//                multipart.getParamMap()
//                        .forEach((k, v) -> {
//                            if (v.length == 1) {
//                                map.put(k, v[0]);
//                            } else {
//                                map.put(k, v);
//                            }
//                        });
//                BeanUtil.fillBeanWithMap(map, o, CopyOptions.create());
//                paramDefinition.setParam(o);
//                return;
//            }
//        }
//
//        for (int i = 0; i < paramDefinitions.length; i++) {
//            ParamDefinition paramDefinition = paramDefinitions[i];
//            String name = paramDefinition.getName();
//            Class type = paramDefinition.getParamType();
//            List<Annotation> annotations = paramDefinition.getAnnotations();
//            //判断当前参数表单上传的是否是文件
//            List<UploadFile> files = multipart.getFileList(name);
//            if (files != null && !files.isEmpty()) {
//                if (ClassUtil.isAssignable(type, Set.class) || ClassUtil.isAssignable(type, List.class)) {
//                    Collection strings = CollUtil.toCollection(files);
//                    paramDefinition.setParam(strings);
//                    continue;
//                }
//                if (type.isArray()) {
//                    paramDefinition.setParam(files.toArray()); //paramObjs[i] = httpReqParam;
//                    continue;
//                }
//                if (type == UploadFile.class) {
//                    paramDefinition.setParam(files.get(0));
//                    continue;
//                }
//            }
//            List<String> listParam = multipart.getListParam(name);
//            if (ClassUtil.isAssignable(type, Set.class)
//                    || ClassUtil.isAssignable(type, List.class)
//
//            ) {
//                Collection strings = CollUtil.toCollection(listParam);
//                paramDefinition.setParam(strings);
//                continue;
//            }
//            if (type.isArray()) {
//                paramDefinition.setParam(listParam.toArray());
//                continue;
//            }
//
//            if (ClassUtil.isSimpleValueType(type)) {
//                if (listParam.size() == 1) {
//                    Object obj = Convert.convert(type, listParam.get(0));
//                    paramDefinition.setParam(obj);
//                } else {
//                    Object obj = Convert.convert(type, listParam);
//                    paramDefinition.setParam(obj);
//                }
//                continue;
//            }
//        }
//    }
//
//    private void parseFormUrlencode(HttpServletRequest request, ParamDefinition[] paramDefinitions) {
//        Map<String, String[]> httpReqParams = ServletUtil.getParams(request);
//        if (paramDefinitions.length == 1) {
//            ParamDefinition paramDefinition = paramDefinitions[0];
//            String name = paramDefinition.getName();
//            Class<?> type = paramDefinition.getParamType();
//            if (!ClassUtil.isSimpleTypeOrArray(type)
//                    && String.class != type
//                    && ClassUtil.isAssignable(Collection.class, type)) {
//                Object o = ReflectUtil.newInstanceIfPossible(type);
//                Map<String, Object> map = new HashMap<>();
//                httpReqParams.forEach((k, v) -> {
//                    if (v.length == 1) {
//                        map.put(k, v[0]);
//                    } else {
//                        map.put(k, v);
//                    }
//                });
//                BeanUtil.fillBeanWithMap(map, o, CopyOptions.create());
//                paramDefinition.setParam(o);
//                return;
//            }
//        }
//
//
//        for (int i = 0; i < paramDefinitions.length; i++) {
//            ParamDefinition paramDefinition = paramDefinitions[i];
//            String name = paramDefinition.getName();
//            String[] httpReqParam = httpReqParams.get(name);
//            if (httpReqParam == null) {
//                paramDefinition.setParam(null);
//                continue;
//            }
//            Class<?> type = paramDefinition.getParamType();
//            if (ClassUtil.isAssignable(Collection.class, type)) {
//                Collection<Object> strings = CollUtil.toCollection(CollUtil.newArrayList(httpReqParam));
//                paramDefinition.setParam(strings);
//                continue;
//            }
//            if (type.isArray()) {
//                paramDefinition.setParam(httpReqParam);
//                continue;
//            }
//            if (ClassUtil.isSimpleValueType(type)) {
//                if (httpReqParam.length == 1) {
//                    Object obj = Convert.convert(type, httpReqParam[0]);
//                    paramDefinition.setParam(obj);
//                } else {
//                    Object obj = Convert.convert(type, httpReqParam);
//                    paramDefinition.setParam(obj);
//                }
//                continue;
//            }
//        }
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
//}
