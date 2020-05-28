package xyz.xiezc.ioc.system.annotation.handler;

import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.system.annotation.Configuration;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.annotation.Inject;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.definition.*;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;


/**
 * 被configuration 注解的方法的处理逻辑
 */
@Component
public class ConfigurationAnnotationHandler extends AnnotationHandler<Configuration> {

    @Override
    public Class<Configuration> getAnnotationType() {
        return Configuration.class;
    }

    @Inject
    ApplicationContextUtil applicationContextUtil;
    @Inject
    BeanDefinitionContext beanDefinitionContext;

    @Override
    public void processClass(Configuration annotation, Class clazz) {
        BeanDefinition beanDefinition = dealBeanAnnotation(annotation, clazz, applicationContextUtil);
        Class<?> beanClass = getRealBeanClass(beanDefinition);
        beanDefinitionContext.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinition);
        //处理@Bean和@BeanScan注解







    }

    @Override
    public void processMethod(MethodDefinition method, Configuration annotation, BeanDefinition beanSignature) {

    }

    @Override
    public void processField(FieldDefinition field, Configuration annotation, BeanDefinition beanSignature) {

    }


}
