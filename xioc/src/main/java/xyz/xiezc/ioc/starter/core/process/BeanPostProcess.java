package xyz.xiezc.ioc.starter.core.process;

import xyz.xiezc.ioc.starter.core.context.BeanFactory;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

/**
 * @Description 一个bean对象的初始化分成三步
 * 1， 实例化
 * 2， 依赖注入
 * 3， 初始化方法执行
 * 所以中间应该四步钩子方法
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
     * @param beanFactory
     * @param beanDefinition
     * @return true 则会继续往后执行， false: 则中断bean的初始化流程
     */
    boolean beforeInstance(BeanFactory beanFactory, BeanDefinition beanDefinition);

    /**
     * 这个方法在依赖注入之前执行
     *
     * @param beanFactory
     * @param beanDefinition
     * @return true 则会继续往后执行， false: 则中断bean的初始化流程
     */
    boolean beforeInject(BeanFactory beanFactory, BeanDefinition beanDefinition);


    /**
     * 在初始化方法调用之前触发。 配置信息注入和依赖的注入一般是在这个方法触发
     *
     * @param beanFactory
     * @param beanDefinition
     * @return true 则会继续往后执行， false: 则中断bean的初始化流程
     */
    boolean beforeInit(BeanFactory beanFactory, BeanDefinition beanDefinition);


    /**
     * 在初始化方法调用之后触发。  一些面向切面的AOP操作在这个方法中完成。
     * 这个方法调用结束之后， 整个bean才是真正的初始化完成，可以正常使用
     *
     * @param beanFactory
     * @param beanDefinition
     * @return true 则会继续往后执行， false: 则中断bean的初始化流程
     */
    boolean afterInit(BeanFactory beanFactory, BeanDefinition beanDefinition);


}