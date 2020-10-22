package xyz.xiezc.ioc.starter.core.process;

import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 3:03 下午
 **/
public interface BeanPostProcess {

    default int order() {
        return 0;
    }

    /**
     * 这个方法就是反射生成对象
     * 在实例化之前调用，如果已经实例化了，则不会调用这个方法。
     *
     * @param applicationContext
     * @param beanDefinition
     * @return true 则会继续往后执行， false: 则中断bean的初始化流程
     */
    boolean beforeInstance(ApplicationContext applicationContext, BeanDefinition beanDefinition);


    /**
     * 在初始化方法调用之前触发。 配置信息注入和依赖的注入一般是在这个方法触发
     *
     * @param applicationContext
     * @param beanDefinition
     * @return true 则会继续往后执行， false: 则中断bean的初始化流程
     */
    boolean beforeInit(ApplicationContext applicationContext, BeanDefinition beanDefinition);


    /**
     * 在初始化方法调用之后触发。  一些面向切面的AOP操作在这个方法中完成。
     * 这个方法调用结束之后， 整个bean才是真正的初始化完成，可以正常使用
     *
     * @param applicationContext
     * @param beanDefinition
     * @return true 则会继续往后执行， false: 则中断bean的初始化流程
     */
    boolean afterInit(ApplicationContext applicationContext, BeanDefinition beanDefinition);


}