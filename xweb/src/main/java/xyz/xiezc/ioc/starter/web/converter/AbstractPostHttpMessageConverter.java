package xyz.xiezc.ioc.starter.web.converter;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import xyz.xiezc.ioc.starter.web.common.ContentType;

import java.lang.reflect.Parameter;
import java.util.*;

public abstract class AbstractPostHttpMessageConverter extends AbstractHttpMessageConverter {


    protected Map<String, List<String>> getRequestParams(FullHttpRequest request) {
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
}
