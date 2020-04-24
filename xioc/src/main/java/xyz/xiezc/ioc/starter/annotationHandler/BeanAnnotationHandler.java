package xyz.xiezc.ioc.starter.annotationHandler;

import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Bean;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.common.XiocUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

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
        Class beanClass = methodDefinition.getReturnType();
        BeanDefinition beanDefinitionMethod = XiocUtil.dealBeanAnnotation(annotation, beanClass, contextUtil);
        beanDefinitionMethod.setBeanTypeEnum(BeanTypeEnum.methodBean);
        beanDefinitionMethod.setInvokeMethodBean(methodDefinition);
        //MethodBean的特殊性，所以beanName 和class重新设置下
        String beanName = annotation.value();
        if (StrUtil.isBlank(beanName)) {
            beanName = methodDefinition.getMethodName();
        }
        beanDefinitionMethod.setBeanName(beanName);
        beanClass = XiocUtil.getRealBeanClass(beanDefinitionMethod);
        contextUtil.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinitionMethod);
    }


    @Override
    public void processField(FieldDefinition fieldDefinition, Bean annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {

    }
}
