package xyz.xiezc.ioc.common.context;

import xyz.xiezc.ioc.definition.BeanDefinition;

import java.util.Collection;
import java.util.List;

public interface BeanDefinitionContext {


    /**
     * 获取容器中的所有BeanDefinition
     *
     * @return
     */
    Collection<BeanDefinition> getAllBeanDefintion();

    /**
     * 注入bean到容器中
     *
     * @param beanDefinition
     */
    void addBeanDefinition(String beanName, Class<?> beanClass, BeanDefinition beanDefinition);

    /**
     * 获取name和class 都一样的bean
     *
     * @param beanName
     * @param beanClass
     * @return
     */
    BeanDefinition getBeanDefinition(String beanName, Class<?> beanClass);

    /**
     * 根据name和class 获取可以注入的一个bean。
     * 1. name 存在， class 一样 ：  注入
     * 2. name 存在， class有子bean： 选择子bean中name一样的注入
     * 3. name 存在， class没有，子bean也无： 报错
     * 4. name 不存在， class 一样： 注入
     * 5. name 不存在，  class有子bean ： 选择第一个子bean注入
     * 6. name 不存在， class无子bean： 报错
     *
     * @param beanName
     * @param beanClass
     * @return
     */
    BeanDefinition getInjectBeanDefinition(String beanName, Class<?> beanClass);


    /**
     * 获取这个name一样的bean
     *
     * @param beanName
     * @return
     */
    BeanDefinition getBeanDefinition(String beanName);

    /**
     * 获取name一样，但是包含子bean的所有bean
     *
     * @param beanName
     * @return
     */
    List<BeanDefinition> getBeanDefinitions(String beanName);


    /**
     * 获取一样的class的bean
     *
     * @param beanClass
     * @return
     */
    BeanDefinition getBeanDefinition(Class<?> beanClass);

    /**
     * 选择这个class类的bean和这个类型的子bean
     *
     * @param beanClass
     * @return
     */
    List<BeanDefinition> getBeanDefinitions(Class<?> beanClass);

}
