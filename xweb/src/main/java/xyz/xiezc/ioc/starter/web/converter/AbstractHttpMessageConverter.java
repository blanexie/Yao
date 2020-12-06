package xyz.xiezc.ioc.starter.web.converter;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import org.jetbrains.annotations.NotNull;
import xyz.xiezc.ioc.starter.web.common.ContentType;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiezc
 */
public abstract class AbstractHttpMessageConverter implements HttpMessageConverter {


    @NotNull
    public Object[] getControllerParams(Map<String, List<String>> parameters, LinkedHashMap<String, Parameter> paramMap) {
        //方法的参数遍历
        Set<Map.Entry<String, Parameter>> entries = paramMap.entrySet();
        int size = entries.size();
        Object[] result = new Object[size];
        int index = -1;
        for (Map.Entry<String, Parameter> entry : entries) {
            index++;
            //参数名称
            String paramName = entry.getKey();
            //参数名 对应传入的值
            List<String> paramValues = parameters.get(paramName);
            if (paramValues.isEmpty()) {
                result[index] = null;
                continue;
            }
            //参数的类型
            Class<?> paramType = entry.getValue().getType();
            if (ClassUtil.isSimpleValueType(paramType)) {
                //普通类型
                result[index] = Convert.convert(paramType, paramValues.get(0));
            } else if (paramType.isArray()) {
                //数组类型
                Class<?> componentType = paramType.getComponentType();
                List<?> collect = paramValues.stream().map(obj -> Convert.convert(componentType, obj))
                        .collect(Collectors.toList());
                result[index] = collect;
            } else if (ClassUtil.isAssignable(Collection.class, paramType)) {
                ParameterizedType parameterizedType = (ParameterizedType) entry.getValue().getParameterizedType();
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                    //集合类型
                    Collection<Object> collect = paramValues.stream()
                            .map(obj -> Convert.convert(actualTypeArguments[0], obj))
                            .collect(Collectors.toList());
                    result[index] = collect;
                } else {
                    Collection collect = paramValues.stream().collect(Collectors.toList());
                    result[index] = collect;
                }
            } else {
                //其他类型
                result[index] = Convert.convert(paramType, paramValues.get(0));
                continue;
            }
        }
        return result;
    }

}
