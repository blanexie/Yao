package xyz.xiezc.ioc.starter.web.annotation.handler;

import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.web.annotation.Controller;

public class ControllerAnnotationHandler extends AnnotationHandler<Controller> {


    @Override
    public Class<Controller> getAnnotationType() {
        return Controller.class;
    }

    @Override
    public void processClass(Controller annotation, Class clazz, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Controller annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Controller annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
