package xyz.xiezc.ioc.starter.core.context;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanFactoryPostProcess;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 11:27 上午
 **/
public class DefaultApplicationContext extends AbstractApplicationContext {

    @Override
    public void addBean(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(clazz);
        beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        this.singletonBeanDefinitionMap.put(beanDefinition.getBeanClass(), beanDefinition);
    }

    @Override
    public void addBean(BeanDefinition beanDefinition) {
        this.singletonBeanDefinitionMap.put(beanDefinition.getBeanClass(), beanDefinition);
    }

    /**
     * 扫描加载某个目录下的 bean信息， 如果是Configuration注解，还会扫描其中的方法上有无@Bean注解,
     * 不支持 FactoryBean的方式
     *
     * @param packageName
     */
    @Override
    public void loadBeanDefinitions(String packageName) {
        Set<Class<?>> classConfiguration = ClassUtil.scanPackageByAnnotation(packageName, Configuration.class);
        Set<Class<?>> classComponent = ClassUtil.scanPackageByAnnotation(packageName, Component.class);
        classConfiguration.stream().forEach(clazz -> {
            this.addBean(clazz);
        });
        classComponent.stream().forEach(clazz -> {
            this.addBean(clazz);
        });
    }

    @Override
    public void invokeBeanFactoryPostProcess() {
        while (true) {
            //第一步, 筛选出来所有的BeanFactoryPostProcess的实现类
            Set<BeanDefinition> beanFactoryPostProcessSet = singletonBeanDefinitionMap.values().stream()
                    .filter(beanDefinition -> beanDefinition.getBeanStatus() == BeanStatusEnum.Original)
                    .filter(beanDefinition -> {
                        Class<?> beanClass = beanDefinition.getBeanClass();
                        return ClassUtil.isAssignable(BeanFactoryPostProcess.class, beanClass);
                    }).collect(Collectors.toSet());
            if (beanFactoryPostProcessSet.isEmpty()) {
                return;
            }
            //直接反射生成对象。
            for (BeanDefinition beanDefinition : beanFactoryPostProcessSet) {
                Class<?> beanClass = beanDefinition.getBeanClass();
                Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
                beanDefinition.setBean(bean);
                beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
            }
            //先排序BeanFactoryPostProcess的实现类, 再根据顺序逐个调用
            beanFactoryPostProcessSet.stream()
                    .sorted((a, b) -> {
                        BeanFactoryPostProcess bean = a.getBean();
                        BeanFactoryPostProcess bean1 = b.getBean();
                        int order = bean.order();
                        int order1 = bean1.order();
                        return order - order1;
                    }).forEachOrdered(beanDefinition -> {
                BeanFactoryPostProcess bean = beanDefinition.getBean();
                bean.process(this);
            });

        }

    }

}