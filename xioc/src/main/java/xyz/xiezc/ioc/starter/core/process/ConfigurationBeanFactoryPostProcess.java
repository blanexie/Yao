package xyz.xiezc.ioc.starter.core.process;

import cn.hutool.core.annotation.AnnotationUtil;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        //获取容器
        Map<Class<?>, BeanDefinition> singletonBeanDefinitionMap = applicationContext.getSingletonBeanDefinitionMap();
        //遍历容器类，找到所有的Configuration注解的bean
        Set<BeanDefinition> beanDefinitionSet = new HashSet<>();
        singletonBeanDefinitionMap.forEach((k, v) -> {
            Class<?> beanClass = v.getBeanClass();
            Configuration annotation = AnnotationUtil.getAnnotation(beanClass, Configuration.class);
            if (annotation != null) {
                beanDefinitionSet.add(v);
            }
        });
    }
}