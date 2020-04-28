package xyz.xiezc.ioc.starter.web.annotation.handler;

import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.web.annotation.Controller;

@Component
public class ControllerAnnotationHandler extends AnnotationHandler<Controller> {


    @Override
    public Class<Controller> getAnnotationType() {
        return Controller.class;
    }

    @Override
    public void processClass(Controller annotation, Class clazz, ApplicationContextUtil contextUtil) {
        BeanDefinition beanDefinition =dealBeanAnnotation(annotation, clazz, contextUtil);
        Class<?> beanClass =getRealBeanClass(beanDefinition);
        String value = annotation.value();
        if(StrUtil.isNotBlank(value)){
            beanDefinition.setBeanName(value);
        }
        contextUtil.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinition);
    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Controller annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Controller annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
