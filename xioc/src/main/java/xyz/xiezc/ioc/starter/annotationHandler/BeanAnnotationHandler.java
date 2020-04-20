package xyz.xiezc.ioc.starter.annotationHandler;

import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Bean;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.common.XiocUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FactoryBean;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanScopeEnum;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class BeanAnnotationHandler extends AnnotationHandler<Bean> {

    @Override
    public Class<Bean> getAnnotationType() {
        return Bean.class;
    }

    @Override
    public void processClass(Bean annotation, Class clazz, ContextUtil contextUtil) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Bean annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {
        BeanDefinition beanDefinitionMethod = XiocUtil.dealBeanAnnotation(annotation, methodDefinition.getReturnType(), contextUtil);
        beanDefinitionMethod.setBeanScopeEnum(BeanScopeEnum.methodBean);
        beanDefinitionMethod.setInvokeMethodBean(methodDefinition);
        Class<?> beanClass = XiocUtil.getRealBeanClass(beanDefinition);
        contextUtil.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinition);
    }



    @Override
    public void processField(FieldDefinition fieldDefinition, Bean annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {

    }
}
