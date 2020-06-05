package xyz.xiezc.ioc.starter.annotation.handler;


import lombok.Data;
import xyz.xiezc.ioc.starter.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.annotation.Component;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.common.definition.MethodDefinition;

import java.lang.annotation.Annotation;

@Data
public class ComponentAnnotationHandler extends AnnotationHandler<Component> {

    @Override
    public Class<Component> getAnnotationType() {
        return Component.class;
    }


    ApplicationContextUtil applicationContextUtil;

    /**
     * 这个注解的左右就是把bean放入容器中
     *
     * @param annotation 注解
     * @param clazz      被注解的类
     */
    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {
        BeanDefinition beanDefinition1 = dealBeanAnnotation(annotation, clazz, applicationContextUtil);
        Class<?> beanClass = getRealBeanClass(beanDefinition1);
        applicationContextUtil.getBeanDefinitionContext().addBeanDefinition(beanDefinition1.getBeanName(), beanClass, beanDefinition1);
    }

    /**
     * 处理Component 注解的方法
     *
     * @param methodDefinition 被注解的方法
     * @param annotation       这个类上的所有注解
     * @param beanSignature    被注解的类
     */
    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanSignature) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanSignature) {

    }


}
