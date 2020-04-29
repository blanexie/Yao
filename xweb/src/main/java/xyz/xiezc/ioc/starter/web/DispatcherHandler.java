package xyz.xiezc.ioc.starter.web;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.web.entity.HttpHeader;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;
import xyz.xiezc.ioc.starter.web.entity.HttpResponse;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DispatcherHandler {

    Log log = LogFactory.get(DispatcherHandler.class);

    public static Map<String, MethodDefinition> getMethods = new HashMap<>();
    public static Map<String, MethodDefinition> postMethods = new HashMap<>();


    public HttpResponse doRequest(HttpRequest httpRequest) {
        try {
            //找到对应的类
            String method = httpRequest.getMethod();
            MethodDefinition methodDefinition = null;
            if (StrUtil.equalsIgnoreCase(method, "get")) {
                methodDefinition = getMethods.get(httpRequest.getPath());
            } else if (StrUtil.equalsIgnoreCase(method, "post")) {
                methodDefinition = postMethods.get(httpRequest.getPath());
            } else {
                //TODO  return;
            }
            if (methodDefinition == null) {
                //TODO  return;
            }

            //映射方法参数
            Map<String, List<String>> paramMap = httpRequest.getParamMap();
            ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
            List<Object> collect = CollUtil.newArrayList(paramDefinitions).stream()
                    .map(paramDefinition -> {
                        String paramName = paramDefinition.getParamName();
                        List<String> params = paramMap.get(paramName);
                        if (params == null || params.isEmpty()) {
                            return Convert.convert(paramDefinition.getParamType(), null);
                        }
                        return paramMapping(params, paramDefinition);
                    }).collect(Collectors.toList());

            String methodName = methodDefinition.getMethodName();
            BeanDefinition beanDefinition = methodDefinition.getBeanDefinition();
            Object bean = beanDefinition.getBean();
            Object[] objects = collect.toArray();
            Object invoke = ReflectUtil.invoke(bean, methodName, objects);
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setBody(invoke);
            HttpHeader httpHeader = new HttpHeader("Content-Type", "application/json;charset=utf-8");
            httpResponse.setHeaders(CollUtil.newArrayList(httpHeader));
            return httpResponse;
        } catch (Exception e) {
            log.error(e);
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setBody(e.getMessage());
            HttpHeader httpHeader = new HttpHeader("Content-Type", "application/json;charset=utf-8");
            httpResponse.setHeaders(CollUtil.newArrayList(httpHeader));
            return httpResponse;
        }
    }


    public Object paramMapping(List<String> params, ParamDefinition paramDefinition) {
        Class paramType = paramDefinition.getParamType();
        //如果是基本类型， 则把list中的第一个值转换类型，赋值进去
        if (ClassUtil.isSimpleValueType(paramType)) {
            return Convert.convert(paramType, params.get(0));
        }
        //如果是数组类型，
        if (paramType.isArray()) {
            return params.toArray();
        }
        //如果是集合类型
        if (ClassUtil.isAssignable(Collection.class, paramType)) {
            return CollUtil.toCollection(params);
        }
        //如果是其他类型
        if (params.size() == 1) {
            return Convert.convert(paramType, params.get(0));
        }
        return Convert.convert(paramType, params);
    }

}
