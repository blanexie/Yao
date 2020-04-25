package xyz.xiezc.ioc.annotationHandler;

import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.web.annotation.Controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ControllerAnnotationHandler extends AnnotationHandler<Controller> {

    @Override
    public Class<Controller> getAnnotationType() {
        return Controller.class;
    }

    @Override
    public void processClass(Controller annotation, Class clazz, ApplicationContextUtil contextUtil) {
        BeanDefinition beanDefinition = XiocUtil.dealBeanAnnotation(annotation, clazz, contextUtil);
        contextUtil.addBeanDefinition(beanDefinition);
    }

    @Override
    public void processMethod(Method method, Controller annotation, BeanSignature beanSignature, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(Field field, Controller annotation, BeanSignature beanSignature, ApplicationContextUtil contextUtil) {

    }
}
