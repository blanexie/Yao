package xyz.xiezc.ioc.starter.web.converter;

import cn.hutool.core.collection.CollUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.web.common.ContentType;

import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QueryStrHttpMessageConverter extends AbstractHttpMessageConverter {

    @Override
    public List<ContentType> getSupportContentType() {
        return CollUtil.newArrayList(ContentType.Default);
    }

    @Override
    public Object[] parseParamaters(FullHttpRequest request, LinkedHashMap<String, Parameter> paramMap) {
        //解析传入的 值
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        return getControllerParams(parameters, paramMap);
    }

}
