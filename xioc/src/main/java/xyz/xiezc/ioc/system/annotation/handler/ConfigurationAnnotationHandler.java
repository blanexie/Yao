package xyz.xiezc.ioc.system.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.val;
import xyz.xiezc.ioc.system.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.system.annotation.Bean;
import xyz.xiezc.ioc.system.annotation.BeanScan;
import xyz.xiezc.ioc.system.annotation.Configuration;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 被configuration 注解的方法的处理逻辑
 */
@Data
public class ConfigurationAnnotationHandler extends AnnotationHandler<Configuration> {

    @Override
    public Class<Configuration> getAnnotationType() {
        return Configuration.class;
    }

    ApplicationContextUtil applicationContextUtil;


    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition1) {
        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();
        BeanDefinition beanDefinition = dealBeanAnnotation(annotation, clazz, applicationContextUtil);
        beanDefinitionContext.addBeanDefinition(beanDefinition.getBeanName(), getRealBeanClass(beanDefinition), beanDefinition);
        //处理@Bean注解
        Set<MethodDefinition> methodDefinitions = beanDefinition.getMethodDefinitions();
        methodDefinitions.stream()
                .filter(methodDefinition -> {
                    Method method = methodDefinition.getMethod();
                    Bean bean = AnnotationUtil.getAnnotation(method, Bean.class);
                    return bean != null;
                })
                .forEach(methodDefinition -> {
                    Class beanClass = methodDefinition.getReturnType();
                    BeanDefinition beanDefinitionMethod = dealBeanAnnotation(annotation, beanClass, applicationContextUtil);
                    beanDefinitionMethod.setBeanTypeEnum(BeanTypeEnum.methodBean);
                    beanDefinitionMethod.setInvokeMethodBean(methodDefinition);
                    //MethodBean的特殊性，所以beanName 和class重新设置下
                    String beanName = beanDefinitionMethod.getBeanName();
                    if (StrUtil.isBlank(beanName)) {
                        beanName = methodDefinition.getMethod().getName();
                    }
                    beanDefinitionMethod.setBeanName(beanName);
                    beanClass = getRealBeanClass(beanDefinitionMethod);
                    beanDefinitionContext.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinitionMethod);
                });
        //处理@BeanScan注解
        Class<?> beanClass = beanDefinition.getBeanClass();
        BeanScan beanScan = AnnotationUtil.getAnnotation(beanClass, BeanScan.class);
        if (beanScan == null) {
            return;
        }
        Class<?>[] classes = beanScan.basePackageClasses();
        List<String> packages = CollUtil.newArrayList(classes)
                .stream()
                .map(ClassUtil::getPackage)
                .collect(Collectors.toList());
        String[] strings = beanScan.basePackages();
        for (String string : strings) {
            packages.add(string);
        }
        for (String aPackage : packages) {
            applicationContextUtil.loadBeanDefinitions(aPackage);
        }
    }

    @Override
    public void processMethod(MethodDefinition method, Annotation annotation, BeanDefinition beanSignature) {

    }

    @Override
    public void processField(FieldDefinition field, Annotation annotation, BeanDefinition beanSignature) {

    }


}
