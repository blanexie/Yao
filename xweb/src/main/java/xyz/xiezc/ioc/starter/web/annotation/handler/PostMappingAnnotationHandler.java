package xyz.xiezc.ioc.starter.web.annotation.handler;

import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.web.annotation.PostMapping;

public class PostMappingAnnotationHandler extends AnnotationHandler<PostMapping> {

    @Override
    public Class<PostMapping> getAnnotationType() {
        return PostMapping.class;
    }

    @Override
    public void processClass(PostMapping annotation, Class clazz, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, PostMapping annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, PostMapping annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
