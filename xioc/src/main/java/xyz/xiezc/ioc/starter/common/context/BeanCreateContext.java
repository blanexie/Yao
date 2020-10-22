package xyz.xiezc.ioc.starter.common.context;

import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

/**
 * bean创建相关的接口， 包含创建时候的循环依赖的判断。
 */
public interface BeanCreateContext {


    /**
     * 1. 将这个bean放入待初始化集合中，这个主要是用来解决循环依赖的判断的
     * 2. 先初始化这个类。三种类型的有不同的初始化方法
     * 3. 检查有无自定义注解，如果有的话，此时获取对应的annotationHandler来处理下。Inject和Value的注解处理器很重要， 需要判断依赖是否存在
     * 4. 判断有无切面，有切面的话，生成代理类替换生成的bean
     *
     * @param beanDefinition
     * @return
     */
    BeanDefinition createBean(BeanDefinition beanDefinition);


    /**
     *
     * @param beanDefinition
     * @return
     */
    BeanDefinition buildBean(BeanDefinition beanDefinition);







}
