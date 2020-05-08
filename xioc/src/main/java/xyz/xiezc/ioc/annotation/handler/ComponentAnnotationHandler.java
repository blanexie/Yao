package xyz.xiezc.ioc.annotation.handler;


import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.definition.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
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
    public void processClass(Component annotation, Class clazz, ApplicationContextUtil contextUtil) {
        BeanDefinition beanDefinition =dealBeanAnnotation(annotation, clazz, contextUtil);
        Class<?> beanClass =getRealBeanClass(beanDefinition);
        contextUtil.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinition);
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
    public void processMethod(MethodDefinition methodDefinition, Component annotation, BeanDefinition beanSignature, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Component annotation, BeanDefinition beanSignature, ApplicationContextUtil contextUtil) {

    }


}
