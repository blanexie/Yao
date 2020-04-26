package xyz.xiezc.ioc.common.context;

import cn.hutool.core.util.ReflectUtil;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.create.impl.SimpleBeanCreateStategy;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.util.Set;

/**
 * bean创建相关的接口， 包含创建时候的循环依赖的判断。
 */
public interface BeanCreateContext {


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


    /**
     * 开始创建bean
     *
     * @param beanDefinition
     * @return
     */
    BeanDefinition createBean(BeanDefinition beanDefinition);

    /**
     * 检验字段的注入对象
     *
     * @param annotationFiledDefinitions
     */
    void checkFieldDefinitions(Set<FieldDefinition> annotationFiledDefinitions);

    /**
     * 初始化bean的第一步，通过反射生成对象， 同时改变下状态.
     * 单独提取出来是因为很多地方会使用到这个方法
     *
     * @param beanDefinition
     * @return
     */
    BeanDefinition newInstance(BeanDefinition beanDefinition);

    /**
     * 检验方法的参数注入对象
     *
     * @param methodBeanInvoke
     */
    void checkMethodParam(MethodDefinition methodBeanInvoke);

    /**
     * 检查init方法是否符合要求
     *
     * @param methodBeanInvoke
     */
    void checkInitMethod(MethodDefinition methodBeanInvoke);


    /**
     * 调用bean
     *
     * @param beanDefinition
     */
    void doInitMethod(BeanDefinition beanDefinition);

    /**
     * 设置bean的字段属性值
     *
     * @param beanDefinition
     */
     void injectFieldValue(BeanDefinition beanDefinition);
}
