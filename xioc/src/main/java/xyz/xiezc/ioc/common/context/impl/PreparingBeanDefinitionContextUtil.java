package xyz.xiezc.ioc.common.context.impl;

import xyz.xiezc.ioc.common.context.PreparingBeanDefinitionContext;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class PreparingBeanDefinitionContextUtil implements PreparingBeanDefinitionContext {

    /**
     * 正在创建中的bean缓存，用来判断循环依赖的
     */
    Map<String, BeanDefinition> preparingBeanMap = new HashMap<>();
    /**
     *
     */
    Map<BeanTypeEnum, BeanCreateStrategy> beanCreateStrategyMap = new HashMap<>();

    @Override
    public void putBeanCreateStrategy(BeanCreateStrategy beanCreateStrategy) {
        beanCreateStrategyMap.put(beanCreateStrategy.getBeanTypeEnum(), beanCreateStrategy);
    }

    @Override
    public BeanCreateStrategy getBeanCreateStrategy(BeanTypeEnum beanTypeEnum) {
        return beanCreateStrategyMap.get(beanTypeEnum);
    }

    /**
     * 移除正在创建中的bean
     *
     * @param beanDefinition
     */
    public void removeCreatingBeanDefinition(BeanDefinition beanDefinition) {
        preparingBeanMap.remove(beanDefinition.getBeanName());
    }


    @Override
    public boolean isCircularDependenceBeanDefinition(BeanDefinition beanDefinition) {
        if (preparingBeanMap.get(beanDefinition.getBeanName()) == null) {
            preparingBeanMap.put(beanDefinition.getBeanName(), beanDefinition);
            return true;
        }
        return false;
    }
}
