package xyz.xiezc.ioc.system.annotation.handler;


import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.starter.annotation.Inject;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;

public class ComponentAnnotationHandler extends AnnotationHandler<Component> {

    @Override
    public Class<Component> getAnnotationType() {
        return Component.class;
    }

    BeanDefinitionContext beanDefinitionContext;

    ApplicationContextUtil applicationContextUtil;

    /**
     * 这个注解的左右就是把bean放入容器中
     *
     * @param clazz       被注解的类
     * @param annotation  注解
     */
    @Override
    public void processClass(Component annotation, Class clazz) {
        BeanDefinition beanDefinition =dealBeanAnnotation(annotation, clazz, applicationContextUtil);
        Class<?> beanClass =getRealBeanClass(beanDefinition);
        beanDefinitionContext.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinition);
    }

    /**
     * 处理Component 注解的方法
     *
     * @param methodDefinition 被注解的方法
     * @param annotation       这个类上的所有注解
     * @param beanSignature    被注解的类
     */
    @Override
    public void processMethod(MethodDefinition methodDefinition, Component annotation, BeanDefinition beanSignature) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Component annotation, BeanDefinition beanSignature) {

    }


}
