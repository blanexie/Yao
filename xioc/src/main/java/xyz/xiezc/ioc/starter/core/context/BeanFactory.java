package xyz.xiezc.ioc.starter.core.context;

import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.util.Collection;
import java.util.Map;

/**
 * 真正的容器接口
 */
public interface BeanFactory {

    /**
     * 每个容器都有一个唯一ID来区分
     *
     * @return
     */
    String beanFactoryId();


    /**
     * 获取容器中的bean集合
     *
     * @return
     */
    Map<Class<?>, BeanDefinition> getSingletonBeanDefinitionMap();

    /**
     * 获取容器中 bean的definition
     *
     * @param clazz
     * @return
     */
    BeanDefinition getBeanDefinition(Class<?> clazz);

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
     * @return
     */
    Collection<BeanDefinition> getBeanDefinitions(Class<?> clazz);

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