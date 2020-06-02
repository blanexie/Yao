package xyz.xiezc.ioc.starter.starter.web.annotation.handler;

import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.system.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.starter.web.annotation.WebSockerController;

import java.util.List;

@Component
public class WebSocketControllerAnnotationHandler extends AnnotationHandler<WebSockerController> {


    @Override
    public Class<WebSockerController> getAnnotationType() {
        return WebSockerController.class;
    }

    @Override
    public void processClass(WebSockerController annotation, Class clazz, ApplicationContextUtil contextUtil) {
        List<BeanDefinition> beanDefinitions = contextUtil.getBeanDefinitions(clazz);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            String path = annotation.value();
            beanDefinition.setBeanName(path);
        }
    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, WebSockerController annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, WebSockerController annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
