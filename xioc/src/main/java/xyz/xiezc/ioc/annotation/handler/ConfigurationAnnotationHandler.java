package xyz.xiezc.ioc.annotation.handler;

import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.definition.*;


/**
 * 被configuration 注解的方法的处理逻辑
 */
@Component
public class ConfigurationAnnotationHandler extends AnnotationHandler<Configuration> {

    @Override
    public Class<Configuration> getAnnotationType() {
        return Configuration.class;
    }

    @Override
    public void processClass(Configuration annotation, Class clazz, ApplicationContextUtil contextUtil) {
        BeanDefinition beanDefinition =dealBeanAnnotation(annotation, clazz, contextUtil);
        Class<?> beanClass = getRealBeanClass(beanDefinition);
        contextUtil.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinition);
    }


    @Override
    public void processMethod(MethodDefinition method, Configuration annotation, BeanDefinition beanSignature, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition field, Configuration annotation, BeanDefinition beanSignature, ApplicationContextUtil contextUtil) {

    }


}
