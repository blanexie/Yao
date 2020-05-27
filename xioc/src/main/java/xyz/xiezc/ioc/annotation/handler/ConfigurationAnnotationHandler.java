package xyz.xiezc.ioc.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.common.context.BeanCreateContext;
import xyz.xiezc.ioc.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.definition.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


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
    }

    @Override
    public void processMethod(MethodDefinition method, Configuration annotation, BeanDefinition beanSignature) {

    }

    @Override
    public void processField(FieldDefinition field, Configuration annotation, BeanDefinition beanSignature) {

    }


}
