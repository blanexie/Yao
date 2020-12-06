package xyz.xiezc.ioc.starter.web.converter;

import cn.hutool.core.collection.CollUtil;
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

@Component
public class QueryStrHttpMessageConverter extends AbstractHttpMessageConverter {

    @Override
    public List<ContentType> getSupportContentType() {
        return CollUtil.newArrayList(ContentType.Default);
    }


    private Map<String, List<String>> getRequestParams(FullHttpRequest request) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
        List<InterfaceHttpData> httpPostData = decoder.getBodyHttpDatas();
        Map<String, List<String>> params = new HashMap<>();

        for (InterfaceHttpData data : httpPostData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                List<String> valList = params.get(attribute.getName());
                if(valList==null){
                    valList=new ArrayList<>();
                    params.put(attribute.getName(),valList);
                }
                valList.add(attribute.getValue());
            }
        }
        return params;
    }
    @Override
    public Object[] parseParamaters(FullHttpRequest request, LinkedHashMap<String, Parameter> paramMap) {
        ByteBuf content = request.content();
        QueryStringDecoder decoder = new QueryStringDecoder(new String(ByteBufUtil.getBytes(content)));

        return getControllerParams(decoder.parameters(), paramMap);
    }

}