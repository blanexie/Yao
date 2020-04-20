package xyz.xiezc.ioc.starter.annotationHandler;

import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Init;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;

public class InitAnnotationHandler extends AnnotationHandler<Init> {

    @Override
    public Class<Init> getAnnotationType() {
        return Init.class;
    }

    @Override
    public void processClass(Init annotation, Class clazz, ContextUtil contextUtil) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Init annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {
        beanDefinition.setInitMethodDefinition(methodDefinition);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Init annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {

    }
}
