package xyz.xiezc.ioc.starter.core.context;

import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 10:36 上午
 **/
public interface ApplicationContext {


    Map<Class<?>, BeanDefinition> getSingletonBeanDefinitionMap();

    /**
     * 获取bean,
     * 1. 先获取符合这个类型的bean . 如果有多个符合的，会抛出异常。
     * 2. 获取这个类型的子类等符合注入要求的bean， 如果有多个符合的，会抛出异常。
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getBean(Class<?> clazz);

    /**
     * 获取配置文件中的配置信息
     *
     * @param propertyName
     * @return
     */
    String getProperty(String propertyName);

    /**
     * 获取bean,
     * 1. 先获取符合这个类型的bean . 如果有多个符合的，会抛出异常。
     * 2. 获取这个类型的子类等符合注入要求的bean， 如果有多个符合的，会抛出异常。
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> List<T> getBeans(Class<?> clazz);

    /**
     * 加载bean
     *
     * @param clazz
     */
    void addBean(Class<?> clazz);

    void addBean(BeanDefinition beanDefinition);


}