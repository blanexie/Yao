package xyz.xiezc.ioc.system.common.context;

import xyz.xiezc.ioc.system.common.definition.BeanDefinition;

/**
 * bean创建相关的接口， 包含创建时候的循环依赖的判断。
 */
public interface BeanCreateContext {


    /**
     * 开始创建bean
     *
     * @param beanDefinition
     * @return
     */
    BeanDefinition createBean(BeanDefinition beanDefinition);


}
