package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.annotation.Bean;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.starter.annotation.Inject;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;

@Component
public class BeanAnnotationHandler extends AnnotationHandler<Bean> {

    @Override
    public Class<Bean> getAnnotationType() {
        return Bean.class;
    }

    @Override
    public void processClass(Bean annotation, Class clazz) {

    }

    @Inject
    ApplicationContextUtil applicationContextUtil;

    @Inject
    BeanDefinitionContext beanDefinitionContext;

    @Override
    public void processMethod(MethodDefinition methodDefinition, Bean annotation, BeanDefinition beanDefinition) {
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
    public void processField(FieldDefinition fieldDefinition, Bean annotation, BeanDefinition beanDefinition) {

    }
}
