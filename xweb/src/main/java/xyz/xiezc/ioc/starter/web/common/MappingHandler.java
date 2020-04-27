package xyz.xiezc.ioc.starter.web.common;

import lombok.Data;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;

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

    /**
     *
     */
    ParamDefinition[] paramDefinitions;


}
