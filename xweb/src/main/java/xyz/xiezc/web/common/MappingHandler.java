package xyz.xiezc.web.common;

import lombok.Data;
import xyz.xiezc.ioc.definition.BeanDefinition;

import java.lang.reflect.Method;

@Data
public class MappingHandler {
    /**
     * 路径
     */
    String path;

    /**
     * 对应的controller
     */
    BeanDefinition beanDefinition;

    /**
     * 处理方法
     */
    Method method;


}
