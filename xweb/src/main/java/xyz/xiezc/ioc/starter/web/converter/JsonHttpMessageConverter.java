package xyz.xiezc.ioc.starter.web.converter;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.web.annotation.RequestBody;
import xyz.xiezc.ioc.starter.web.common.ContentType;

import java.lang.reflect.Parameter;
import java.util.*;

@Component
public class JsonHttpMessageConverter implements HttpMessageConverter {

    List<ContentType> contentTypes = new ArrayList<>() {{
        add(ContentType.JSON);
    }};

    @Override
    public List<ContentType> getSupportContentType() {
        return contentTypes;
    }


    @Override
    public Object[] parseParamaters(FullHttpRequest httpRequest, LinkedHashMap<String, Parameter> paramMap) {
        ByteBuf content = httpRequest.content();
        String body = new String(  ByteBufUtil.getBytes(content), CharsetUtil.CHARSET_UTF_8);
        Set<Map.Entry<String, Parameter>> entries = paramMap.entrySet();
        Object[] result = new Object[entries.size()];
        int index = -1;
        boolean isContinue = false;
        for (Map.Entry<String, Parameter> entry : entries) {
            index++;
            if (isContinue) {
                continue;
            }
            Parameter value = entry.getValue();
            RequestBody requestBody = AnnotationUtil.getAnnotation(value, RequestBody.class);
            if (requestBody == null) {
                result[index] = null;
            } else {
                Object param = null;
                Class<?> paramType = entry.getValue().getType();
                if (ClassUtil.isSimpleValueType(paramType)) {

                } else if (paramType.isArray()) {
                    JSONArray objects = JSONUtil.parseArray(body);
                    param = objects.toArray();
                } else if (ClassUtil.isAssignable(Collection.class, paramType)) {
                    JSONArray objects = JSONUtil.parseArray(body);
                    param = CollUtil.toCollection(objects);
                } else {
                    //其他类型
                    param = JSONUtil.toBean(body, paramType);
                }
                result[index] = param;
                isContinue = true;
            }

        }

        return result;
    }

}
