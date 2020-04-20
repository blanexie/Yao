package xyz.xiezc.ioc.starter.annotationHandler;

import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.web.annotation.GetMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GetMappingAnnotationHandler extends AnnotationHandler<GetMapping> {
    @Override
    public Class<GetMapping> getAnnotationType() {
        return GetMapping.class;
    }

    @Override
    public void processClass(GetMapping annotation, Class clazz, ContextUtil contextUtil) {

    }

    @Override
    public void processMethod(Method method, GetMapping annotation, BeanSignature beanSignature, ContextUtil contextUtil) {
        //获取controller
        PostMappingAnnotationHandler.dealMappingMethod(method, beanSignature, contextUtil, annotation.value());
    }

    @Override
    public void processField(Field field, GetMapping annotation, BeanSignature beanSignature, ContextUtil contextUtil) {

    }
}
