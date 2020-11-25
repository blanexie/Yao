package xyz.xiezc.ioc.starter.web.entity;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class RequestDefinition {

    String path;
    HttpMethod httpMethod;

    Annotation annotation;

    BeanDefinition beanDefinition;

    Method invokeMethod;

    /**
     * 参数
     */
    LinkedHashMap<String, Parameter> parameterMap=new LinkedHashMap<>();


}
