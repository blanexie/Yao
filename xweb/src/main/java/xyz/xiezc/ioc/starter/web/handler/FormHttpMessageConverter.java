package xyz.xiezc.ioc.starter.web.handler;

import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.web.common.ContentType;
import xyz.xiezc.ioc.starter.web.common.WebContext;
import xyz.xiezc.ioc.starter.web.entity.FileUpload;
import xyz.xiezc.ioc.starter.web.entity.HttpContent;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Object[] doRead(MethodDefinition methodDefinition, ContentType contentType, HttpRequest request) throws IOException {
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        if (contentType == ContentType.FORM_URLENCODED) {
            Object[] objects = parseFormUrlencode(request, paramDefinitions);
            return objects;
        }
        if (contentType == ContentType.MULTIPART) {
            Object[] objects = parseFormData(request, paramDefinitions);
            return objects;
        }
        return null;
    }




    private Object[] parseFormData(HttpRequest request, ParamDefinition[] paramDefinitions) {
        WebContext webContext = WebContext.get();
        Object[] res = new Object[paramDefinitions.length];
        Map<String, List<HttpContent>> paramMap = request.getBodyList().stream()
                .filter(httpContent -> {
                    boolean isFileUpload = httpContent.getHttpDataType() == HttpContent.HttpDataType.FileUpload;
                    if (isFileUpload) {
                        FileUpload fileUpload = httpContent.getFileUpload();
                        webContext.getFileUploads().add(fileUpload);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.groupingBy(HttpContent::getName));

        for (int i = 0; i < paramDefinitions.length; i++) {
            ParamDefinition paramDefinition = paramDefinitions[i];
            String name = paramDefinition.getParamName();
            List<HttpContent> strings = paramMap.get(name);
            if (strings == null) {
                res[i] = null;
                continue;
            }
            List<String> collect = strings.stream()
                    .filter(httpContent -> {
                        boolean isFileUpload = httpContent.getHttpDataType() == HttpContent.HttpDataType.FileUpload;
                        if (isFileUpload) {
                            FileUpload fileUpload = httpContent.getFileUpload();
                            webContext.getFileUploads().add(fileUpload);
                            return false;
                        }
                        return true;
                    })
                    .map(HttpContent::getValue)
                    .collect(Collectors.toList());

            Object o1 = paramMapping(collect, paramDefinition);
            res[i] = o1;
        }
        return res;
    }

    private Object[] parseFormUrlencode(HttpRequest request, ParamDefinition[] paramDefinitions) {
        Object[] res = new Object[paramDefinitions.length];
        Map<String, List<HttpContent>> paramMap = request.getBodyList().stream()
                .collect(Collectors.groupingBy(HttpContent::getName));
        for (int i = 0; i < paramDefinitions.length; i++) {
            ParamDefinition paramDefinition = paramDefinitions[i];
            String name = paramDefinition.getParamName();
            List<HttpContent> strings = paramMap.get(name);
            List<String> collect = strings.stream().map(HttpContent::getValue).collect(Collectors.toList());
            Object o1 = paramMapping(collect, paramDefinition);
            res[i] = o1;
        }
        return res;
    }


}
