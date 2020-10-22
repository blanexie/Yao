package xyz.xiezc.ioc.starter.core.context;

import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.util.Map;

/**
 * @Description
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 10:36 上午
 **/
public interface ApplicationContext {


    Map<Class<?>, BeanDefinition> getSingletonBeanDefinitionMap();

    /**
     * 获取bean
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getBean(Class<?> clazz);

    /**
     * 加载bean
     *
     * @param clazz
     */
    void addBean(Class<?> clazz);

    void addBean(BeanDefinition beanDefinition);


}