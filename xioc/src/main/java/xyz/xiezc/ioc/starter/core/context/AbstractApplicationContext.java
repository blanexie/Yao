package xyz.xiezc.ioc.starter.core.context;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.setting.Setting;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.starter.core.BeanCreateUtil;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.exception.ManyBeanException;
import xyz.xiezc.ioc.starter.exception.NoSuchBeanException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    /**
     * 配置文件信息
     */
    @Setter
    @Getter
    protected Setting setting = new Setting();

    @Override
    public Map<Class<?>, BeanDefinition> getSingletonBeanDefinitionMap() {
        return singletonBeanDefinitionMap;
    }


    @Override
    public <T> T getBean(Class<?> clazz) {
        BeanCreateUtil instacne = BeanCreateUtil.getInstacne();
        BeanDefinition beanDefinition = singletonBeanDefinitionMap.get(clazz);
        if (beanDefinition != null) {
            instacne.createAndInjectBean(beanDefinition, this);
            return beanDefinition.getBean();
        }
        Set<Class<?>> collect = singletonBeanDefinitionMap.keySet().stream()
                .filter(clazzss -> ClassUtil.isAssignable(clazz, clazzss))
                .collect(Collectors.toSet());
        if (collect.isEmpty()) {
            throw new NoSuchBeanException("容器中未找到符合要求的bean");
        }
        if (collect.size() > 1) {
            throw new ManyBeanException("容器中找到多个符合要求的bean");
        }
        for (Class<?> aClass : collect) {
            BeanDefinition beanDefinition1 = singletonBeanDefinitionMap.get(aClass);
            instacne.createAndInjectBean(beanDefinition1, this);
            return beanDefinition1.getBean();
        }
        throw new NoSuchBeanException("容器中未找到符合要求的bean");
    }

    @Override
    public <T> List<T> getBeans(Class<?> clazz) {
        BeanCreateUtil instacne = BeanCreateUtil.getInstacne();
        List<T> result = new ArrayList<>();
        singletonBeanDefinitionMap.forEach((k, v) -> {
            if (ClassUtil.isAssignable(clazz, k)) {
                instacne.createAndInjectBean(v, this);
                result.add(v.getBean());
            }
        });
        if (result.isEmpty()) {
            throw new NoSuchBeanException("容器中未找到符合要求的bean");
        }
        return result;
    }

    /**
     * 扫描路径下的所有符合注解的类加入到容器中
     *
     * @param packageName
     */
    public abstract void loadBeanDefinitions(String packageName);

    /**
     * 调用所有的初步钩子方法
     */
    public abstract void invokeBeanFactoryPostProcess();

}