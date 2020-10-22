package xyz.xiezc.ioc.starter.core.definition;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;


@Data
public class MethodDefinition {

    /**
     * 方法所在的bean
     */
    private BeanDefinition beanDefinition;



    /**
     * 方法
     */
    Method method;

}
