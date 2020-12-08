package xyz.xiezc.ioc.starter.web.converter;

import io.netty.handler.codec.http.FullHttpRequest;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.web.common.ContentType;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * multipart/form-data 类型的请求解析
 * @author xiezc
 */
@Component
public class MultipartFormHttpMessageConverter extends AbstractPostHttpMessageConverter {

    List<ContentType> contentTypes = new ArrayList<>() {{
        add(ContentType.MULTIPART);
    }};

    @Override
    public List<ContentType> getSupportContentType() {
        return contentTypes;
    }

    @Override
    public Object[] parseParamaters(FullHttpRequest request, LinkedHashMap<String, Parameter> paramMap) {
        Map<String, List<String>> parameters = getRequestParams(request);
        return getControllerParams(parameters, paramMap);
    }

}
