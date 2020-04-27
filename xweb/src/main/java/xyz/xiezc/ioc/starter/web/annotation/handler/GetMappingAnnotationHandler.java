package xyz.xiezc.ioc.starter.web.annotation.handler;

import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;

public class GetMappingAnnotationHandler extends AnnotationHandler<GetMapping> {

    @Override
    public Class<GetMapping> getAnnotationType() {
        return GetMapping.class;
    }

    @Override
    public void processClass(GetMapping annotation, Class clazz, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, GetMapping annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, GetMapping annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
