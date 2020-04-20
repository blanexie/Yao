package xyz.xiezc.ioc.starter.annotationHandler;

import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Bean;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.common.XiocUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanScopeEnum;

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
        beanDefinitionMethod.setMethodBeanInvoke(methodDefinition);
        contextUtil.addBeanDefinition(beanDefinitionMethod);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Bean annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {

    }
}
