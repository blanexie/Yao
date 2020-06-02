package xyz.xiezc.ioc.system.annotation.handler;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import lombok.Data;
import xyz.xiezc.ioc.system.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.Aop;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.system.annotation.Inject;
import xyz.xiezc.ioc.system.common.AopAspect;
import xyz.xiezc.ioc.system.common.context.BeanCreateContext;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

@Data
public class AopAnnotationHandler extends AnnotationHandler<Aop> {

    @Override
    public Class<Aop> getAnnotationType() {
        return Aop.class;
    }

    BeanCreateContext beanCreateContext;

    BeanDefinitionContext beanDefinitionContext;

    @Override
    public void processClass(Annotation annotation, Class clazz,BeanDefinition beanDefinition1) {
        Class<?> aopClass =((Aop)annotation).value();// AnnotationUtil.getAnnotationValue((AnnotatedElement) annotation, annotation.annotationType());
        //获取切面类
        BeanDefinition aopAspectBeanDefinition = beanDefinitionContext.getBeanDefinition(aopClass);
        beanCreateContext.createBean(aopAspectBeanDefinition);
        AopAspect aspectBean = aopAspectBeanDefinition.getBean();
        //获取所有被切的方法
        Method[] publicMethods = ClassUtil.getPublicMethods(clazz);
        for (Method publicMethod : publicMethods) {
            aspectBean.addMethod(publicMethod);
        }
        //获取bean实例
        BeanDefinition beanDefinition = beanDefinitionContext.getBeanDefinition(clazz);
        beanCreateContext.createBean(aopAspectBeanDefinition);
        Object parentBean = beanDefinition.getBean();
        beanDefinition.setBean(ProxyUtil.proxy(parentBean, aspectBean));
    }


    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {
        //获取切面类
        Class<?> aopClass = ((Aop)annotation).value();
        BeanDefinition aopAspectBeanDefinition = beanDefinitionContext.getBeanDefinition(aopClass);
        beanCreateContext.createBean(aopAspectBeanDefinition);
        AopAspect aspectBean = aopAspectBeanDefinition.getBean();
        //获取被切方法
        Method method = methodDefinition.getMethod();
        if (ClassUtil.isPublic(method)) {
            aspectBean.addMethod(method);
        }
        //设置bean实例
        BeanDefinition parentBeanDefinition = methodDefinition.getBeanDefinition();
        beanCreateContext.createBean(aopAspectBeanDefinition);
        Object parentBean = parentBeanDefinition.getBean();
        beanDefinition.setBean(ProxyUtil.proxy(parentBean, aspectBean));
    }


    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }
}
