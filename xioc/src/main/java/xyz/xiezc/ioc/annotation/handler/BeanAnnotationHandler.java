package xyz.xiezc.ioc.annotation.handler;

import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Bean;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

@Component
public class BeanAnnotationHandler extends AnnotationHandler<Bean> {

    @Override
    public Class<Bean> getAnnotationType() {
        return Bean.class;
    }

    @Override
    public void processClass(Bean annotation, Class clazz, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Bean annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {
        Class beanClass = methodDefinition.getReturnType();
        BeanDefinition beanDefinitionMethod = dealBeanAnnotation(annotation, beanClass, contextUtil);
        beanDefinitionMethod.setBeanTypeEnum(BeanTypeEnum.methodBean);
        beanDefinitionMethod.setInvokeMethodBean(methodDefinition);
        //MethodBean的特殊性，所以beanName 和class重新设置下
        String beanName = annotation.value();
        if (StrUtil.isBlank(beanName)) {
            beanName = methodDefinition.getMethodName();
        }
        beanDefinitionMethod.setBeanName(beanName);
        beanClass = getRealBeanClass(beanDefinitionMethod);
        contextUtil.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinitionMethod);
    }


    @Override
    public void processField(FieldDefinition fieldDefinition, Bean annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
