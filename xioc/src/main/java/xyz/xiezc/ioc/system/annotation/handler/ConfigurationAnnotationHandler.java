package xyz.xiezc.ioc.system.annotation.handler;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.system.annotation.Configuration;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;


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
    public void processClass(Configuration annotation, Class clazz) {
        BeanDefinition beanDefinition = dealBeanAnnotation(annotation, clazz, applicationContextUtil);
        Class<?> beanClass = getRealBeanClass(beanDefinition);
        applicationContextUtil.getBeanDefinitionContext().addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinition);
        //处理@Bean和@BeanScan注解






        Class beanClass = methodDefinition.getReturnType();
        BeanDefinition beanDefinitionMethod = dealBeanAnnotation(annotation, beanClass, applicationContextUtil);
        beanDefinitionMethod.setBeanTypeEnum(BeanTypeEnum.methodBean);
        beanDefinitionMethod.setInvokeMethodBean(methodDefinition);
        //MethodBean的特殊性，所以beanName 和class重新设置下
        String beanName = annotation.value();
        if (StrUtil.isBlank(beanName)) {
            beanName = methodDefinition.getMethod().getName();
        }
        beanDefinitionMethod.setBeanName(beanName);
        beanClass = getRealBeanClass(beanDefinitionMethod);
        beanDefinitionContext.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinitionMethod);

    }

    @Override
    public void processMethod(MethodDefinition method, Configuration annotation, BeanDefinition beanSignature) {

    }

    @Override
    public void processField(FieldDefinition field, Configuration annotation, BeanDefinition beanSignature) {

    }


}
