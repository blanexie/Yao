package xyz.xiezc.ioc.starter.web.converter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.web.common.ContentType;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * application/x-www-form-urlencoded 的post 请求的解析
 */
@Component
public class UrlencodedFormHttpMessageConverter extends AbstractHttpMessageConverter {

    List<ContentType> contentTypes = new ArrayList<>() {{
        add(ContentType.FORM_URLENCODED);
    }};

    @Override
    public List<ContentType> getSupportContentType() {
        return contentTypes;
    }

    @Override
    public Object[] parseParamaters(FullHttpRequest request, LinkedHashMap<String, Parameter> paramMap) {
        ByteBuf content = request.content();
        QueryStringDecoder decoder = new QueryStringDecoder(new String(ByteBufUtil.getBytes(content)));
        return getControllerParams(decoder.parameters(), paramMap);
    }


}
