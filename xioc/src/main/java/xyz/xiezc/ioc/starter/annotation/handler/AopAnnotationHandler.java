package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.core.util.ClassUtil;
import lombok.Data;
import xyz.xiezc.ioc.starter.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.annotation.Aop;
import xyz.xiezc.ioc.starter.annotation.SystemLoad;
import xyz.xiezc.ioc.starter.common.AopAspect;
import xyz.xiezc.ioc.starter.common.context.BeanCreateContext;
import xyz.xiezc.ioc.starter.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.common.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Data
@SystemLoad
public class AopAnnotationHandler extends AnnotationHandler<Aop> {

    @Override
    public Class<Aop> getAnnotationType() {
        return Aop.class;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }


    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {
        ApplicationContextUtil applicationContextUtil = Xioc.getApplicationContext();
        BeanCreateContext beanCreateContext = applicationContextUtil.getBeanCreateContext();
        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();

        Class<?> aopClass = ((Aop) annotation).value();// AnnotationUtil.getAnnotationValue((AnnotatedElement) annotation, annotation.annotationType());
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
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original || beanDefinition.getBean() == null) {
            beanCreateContext.createBean(beanDefinition);
        }
        Object parentBean = beanDefinition.getBean();
        beanDefinition.setBean(ProxyUtil.proxy(parentBean, aspectBean));
    }


    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {
        ApplicationContextUtil applicationContextUtil = Xioc.getApplicationContext();
        BeanCreateContext beanCreateContext = applicationContextUtil.getBeanCreateContext();
        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();
        //获取切面类
        Class<?> aopClass = ((Aop) annotation).value();
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
