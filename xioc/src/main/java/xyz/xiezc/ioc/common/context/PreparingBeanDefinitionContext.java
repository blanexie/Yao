package xyz.xiezc.ioc.common.context;

import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.create.impl.SimpleBeanCreateStategy;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

/**
 * bean创建相关的接口， 包含创建时候的循环依赖的判断。
 */
public interface PreparingBeanDefinitionContext {


    /**
     * 增加一种bean的类型创建策略
     *
     * @param beanCreateStrategy
     */
    void putBeanCreateStrategy(BeanCreateStrategy beanCreateStrategy);

    /**
     * 获取一种bean的类型创建策略
     *
     * @param beanTypeEnum
     */
    BeanCreateStrategy getBeanCreateStrategy(BeanTypeEnum beanTypeEnum);

    /**
     * 移除正在创建中的bean
     *
     * @param beanDefinition
     */
    void removeCreatingBeanDefinition(BeanDefinition beanDefinition);

    /**
     * 判断BeanDefinition是否是循环依赖， 同时也把当前的BeanDefinition放入正在创建的bean的缓存中
     *
     * @param beanDefinition
     * @return
     */
    boolean isCircularDependenceBeanDefinition(BeanDefinition beanDefinition);

}
