package xyz.xiezc.ioc.starter.core.process;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import xyz.xiezc.ioc.starter.annotation.core.Bean;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
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
        //处理所有的@Bean注解的方法
        for (BeanDefinition beanDefinition : beanDefinitionSet) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            List<Method> publicMethods = ClassUtil.getPublicMethods(beanClass, method -> {
                Bean annotation = AnnotationUtil.getAnnotation(method, Bean.class);
                return annotation != null;
            });
            //放入容器
            for (Method publicMethod : publicMethods) {
                BeanDefinition methodBeanDefinition = new BeanDefinition();
                methodBeanDefinition.setBeanClass(publicMethod.getReturnType());
                methodBeanDefinition.setBeanTypeEnum(BeanTypeEnum.methodBean);
                MethodDefinition methodDefinition = new MethodDefinition();
                methodDefinition.setMethod(publicMethod);
                methodDefinition.setBeanDefinition(beanDefinition);
                methodBeanDefinition.setInvokeMethodBean(methodDefinition);
                applicationContext.addBean(methodBeanDefinition);
            }
        }

    }
}