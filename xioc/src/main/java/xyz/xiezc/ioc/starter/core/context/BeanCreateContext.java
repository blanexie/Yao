package xyz.xiezc.ioc.starter.core.context;

import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

public interface BeanCreateContext {

    /**
     * 创建对象
     *
     * @param beanDefinition
     * @param <T>
     * @return
     */
    <T> T createCompletedBean(BeanDefinition beanDefinition);

    /**
     * 判断对象是否在创建中
     */
    boolean isBuilding(BeanDefinition beanDefinition);


}
