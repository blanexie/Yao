package xyz.xiezc.ioc.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.annotation.*;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.definition.AnnotationAndHandler;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
