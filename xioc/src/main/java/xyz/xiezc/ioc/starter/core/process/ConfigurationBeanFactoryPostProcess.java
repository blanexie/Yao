package xyz.xiezc.ioc.starter.core.process;

import cn.hutool.core.annotation.AnnotationUtil;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.core.ComponentScan;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.util.*;

/**
 * @Description
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 2:32 下午
 **/
@Component
public class ConfigurationBeanFactoryPostProcess implements BeanFactoryPostProcess {

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void process(ApplicationContext applicationContext) {
        Set<BeanDefinition> beanDefinitionSet = new HashSet<>();
        while (true) {
            List<ComponentScan> componentScanList = new ArrayList<>();
            //获取容器
            Map<Class<?>, BeanDefinition> singletonBeanDefinitionMap = applicationContext.getSingletonBeanDefinitionMap();
            int size = singletonBeanDefinitionMap.size();
            //遍历容器类，找到所有的Configuration注解的bean
            singletonBeanDefinitionMap.forEach((k, v) -> {
                if (!beanDefinitionSet.add(v)) {
                    return;
                }
                Class<?> beanClass = v.getBeanClass();
                Configuration annotation = AnnotationUtil.getAnnotation(beanClass, Configuration.class);
                ComponentScan componentScan = AnnotationUtil.getAnnotation(beanClass, ComponentScan.class);
                if (annotation != null && componentScan != null) {
                    componentScanList.add(componentScan);
                }
            });
            //处理其上的@ComponentScan注解
            for (ComponentScan componentScan : componentScanList) {
                Class<?>[] classes = componentScan.basePackageClasses();
                for (Class<?> aClass : classes) {
                    applicationContext.loadBeanDefinition(aClass.getPackageName());
                }
                String[] strings = componentScan.basePackages();
                for (String string : strings) {
                    applicationContext.loadBeanDefinition(string);
                }
            }

            int afterSize = applicationContext.getSingletonBeanDefinitionMap().size();
            if (afterSize == size) {
                break;
            }
        }
    }
}