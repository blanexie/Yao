package xyz.xiezc.ioc.starter.annotationHandler;


import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.common.XiocUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ComponentAnnotationHandler extends AnnotationHandler<Component> {

    @Override
    public Class<Component> getAnnotationType() {
        return Component.class;
    }

    /**
     * 这个注解的左右就是把bean放入容器中
     *
     * @param clazz       被注解的类
     * @param annotation  注解
     * @param contextUtil 容器中已有的所有bean信息
     */
    @Override
    public void processClass(Component annotation, Class clazz, ContextUtil contextUtil) {
        BeanDefinition beanDefinition = XiocUtil.dealBeanAnnotation(annotation, clazz, contextUtil);
        contextUtil.addBeanDefinition(beanDefinition);
    }


    /**
     * 处理Component 注解的方法
     *
     * @param methodDefinition 被注解的方法
     * @param annotation       这个类上的所有注解
     * @param beanSignature    被注解的类
     * @param contextUtil      容器中已有的所有bean信息
     */
    @Override
    public void processMethod(MethodDefinition methodDefinition, Component annotation, BeanDefinition beanSignature, ContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Component annotation, BeanDefinition beanSignature, ContextUtil contextUtil) {

    }


}
