package xyz.xiezc.ioc.starter.core.context;

import cn.hutool.core.lang.Assert;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 11:08 上午
 **/
public abstract class AbstractApplicationContext implements ApplicationContext {

    /**
     * 初始化完成的 beanDefinition, 依赖已经注入了，初始化方法已经调用了
     */
    protected Map<Class<?>, BeanDefinition> singletonBeanDefinitionMap = new ConcurrentHashMap<>();

    @Override
    public Map<Class<?>, BeanDefinition> getSingletonBeanDefinitionMap() {
        return singletonBeanDefinitionMap;
    }


    @Override
    public <T> T getBean(Class<?> clazz) {
        BeanDefinition beanDefinition = singletonBeanDefinitionMap.get(clazz);
        Assert.notNull(beanDefinition, "容器中不存在这个bean");
        return beanDefinition.getBean();
    }

    /**
     * 扫描路径下的所有符合注解的类加入到容器中
     *
     * @param packageName
     */
    abstract void loadBeanDefinitions(String packageName);

    /**
     * 调用所有的初步钩子方法
     */
    abstract void invokeBeanFactoryPostProcess();

}