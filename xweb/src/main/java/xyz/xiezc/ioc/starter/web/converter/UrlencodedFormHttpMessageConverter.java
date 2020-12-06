package xyz.xiezc.ioc.starter.web.converter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.web.common.ContentType;

import java.lang.reflect.Parameter;
import java.util.*;

/**
 * application/x-www-form-urlencoded 的post 请求的解析
 */
@Component
public class UrlencodedFormHttpMessageConverter extends AbstractPostHttpMessageConverter {

    List<ContentType> contentTypes = new ArrayList<>() {{
        add(ContentType.FORM_URLENCODED);
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
