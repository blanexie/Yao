package xyz.xiezc.ioc.starter.common.context.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import xyz.xiezc.ioc.starter.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class BeanDefinitionContextUtil implements BeanDefinitionContext {

    Log log = LogFactory.get(BeanDefinitionContextUtil.class);

    /**
     * bean的name和class的映射关系
     */
    Map<String, Class<?>> nameAndClassMap = new HashMap<>();

    /**
     * bean的class与Beandefinition的映射关系
     */
    Map<Class<?>, BeanDefinition> classAndBeanDefinitionMap = new HashMap<>();

    @Override
    public List<BeanDefinition> getAllBeanDefintion() {
        Collection<BeanDefinition> values = classAndBeanDefinitionMap.values();
        return CollectionUtil.newArrayList(values);
    }

    /**
     * 注入bean到容器中
     *
     * @param beanDefinition
     */
    @Override
    public void addBeanDefinition(String beanName, Class<?> beanClass, BeanDefinition beanDefinition) {
        if (!beanDefinition.checkBean()) {
            log.error("请注入正确的bean到容器中");
            throw new RuntimeException("请注入正确的bean到容器中, bean: " + beanDefinition.toString());
        }
        nameAndClassMap.put(beanName, beanClass);
        classAndBeanDefinitionMap.put(beanClass, beanDefinition);
    }

    /**
     * 获取name和class 都一样的bean
     *
     * @param beanName
     * @param beanClass
     * @return
     */
    @Override
    public BeanDefinition getBeanDefinition(String beanName, Class<?> beanClass) {
        Class<?> aClass = nameAndClassMap.get(beanName);
        if (aClass == null) {
            return null;
        }
        if (ClassUtil.isAssignable(beanClass, aClass)) {
            return classAndBeanDefinitionMap.get(beanClass);
        }
        return null;
    }

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
    @Override
    public BeanDefinition getInjectBeanDefinition(String beanName, Class<?> beanClass) {
        Class<?> aClass = nameAndClassMap.get(beanName);
        if (aClass == null) {
            BeanDefinition beanDefinition = getBeanDefinition(beanClass);
            if (beanDefinition != null) {
                // 4. name 不存在， class 一样： 注入
                return beanDefinition;
            }

            List<BeanDefinition> beanDefinitions = getBeanDefinitions(beanClass);
            if (beanDefinitions == null || beanDefinitions.isEmpty()) {
                // 6. name 不存在， class无子bean： 报错
                throw new RuntimeException("根据beanName=" + beanName + "和 beanCLass=" + beanClass + "没有找到要注入的bean");
            }
            // 5. name 不存在，  class有子bean ： 选择第一个子bean注入
            return beanDefinitions.get(0);
        } else {
            BeanDefinition beanDefinition = classAndBeanDefinitionMap.get(beanClass);
            if (beanDefinition != null) {
                // 1. name存在， class 一样 ：  注入
                return beanDefinition;
            }
            //寻找这个类 的所有 bean
            List<BeanDefinition> beanDefinitions = getBeanDefinitions(aClass);
            if (beanDefinitions == null || beanDefinitions.isEmpty()) {
                // 3. name 存在， class没有，子bean也无： 报错
                throw new RuntimeException("beanName:"
                        + beanName
                        + ", objClass:"
                        + beanClass.getName()
                        + "没有找到符合要求的bean");
            }
            //2. name 存在， class有子bean： 选择子bean中name一样的注入
            return beanDefinitions.get(0);
        }
    }


    /**
     * 获取这个name一样的bean
     *
     * @param beanName
     * @return
     */
    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        Class<?> aClass = nameAndClassMap.get(beanName);
        if (aClass == null) {
            return null;
        }
        BeanDefinition beanDefinition = classAndBeanDefinitionMap.get(aClass);
        return beanDefinition;
    }

    /**
     * 获取name一样，但是包含子bean的所有bean
     *
     * @param beanName
     * @return
     */
    @Override
    public List<BeanDefinition> getBeanDefinitions(String beanName) {
        Class<?> aClass = nameAndClassMap.get(beanName);
        if (aClass == null) {
            return null;
        }
        return getBeanDefinitions(aClass);
    }


    /**
     * 获取一样的class的bean
     *
     * @param beanClass
     * @return
     */
    @Override
    public BeanDefinition getBeanDefinition(Class<?> beanClass) {
        //先获取最契合的
        BeanDefinition beanDefinition = classAndBeanDefinitionMap.get(beanClass);
        if (beanDefinition != null) {
            return beanDefinition;
        }
        return null;
    }

    /**
     * 选择这个class类的bean和这个类型的子bean
     *
     * @param beanClass
     * @return
     */
    @Override
    public List<BeanDefinition> getBeanDefinitions(Class<?> beanClass) {
        Set<Class<?>> classes = classAndBeanDefinitionMap.keySet();
        List<BeanDefinition> collect = classes.stream()
                .filter(clazz -> ClassUtil.isAssignable(beanClass, clazz))
                .map(classAndBeanDefinitionMap::get)
                .collect(Collectors.toList());
        return collect;
    }
}
