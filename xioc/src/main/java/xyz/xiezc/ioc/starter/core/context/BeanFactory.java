package xyz.xiezc.ioc.starter.core.context;

import cn.hutool.core.util.IdUtil;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * 真正的容器接口
 */
public interface BeanFactory {

    /**
     * 每个容器都有一个唯一ID来区分
     * @return
     */
    default String beanFactoryId() {
        return IdUtil.simpleUUID();
    }


    /**
     * 获取容器中的bean集合
     *
     * @return
     */
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
    <T> T getProperty(String propertyName);

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
     * 获取可注入次字段的直接bean对象
     *
     * @param field
     * @param <T>
     * @return
     */
    <T> T getBean(Field field);

    /**
     * 获取可以注入指定参数的bean类。  会自动根据参数的类型调整注入的bean
     *
     * @param parameter
     * @param <T>
     * @return
     */
    <T> T getBean(Parameter parameter);

    /**
     * 加载bean
     *
     * @param clazz
     */
    void addBean(Class<?> clazz);

    /**
     * 新增bean信息到容器中
     *
     * @param beanDefinition
     */
    void addBean(BeanDefinition beanDefinition);

}
