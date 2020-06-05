package xyz.xiezc.ioc.starter.starter.web.annotation.handler;

import xyz.xiezc.ioc.starter.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.annotation.Component;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.common.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.starter.web.annotation.WebSockerController;

import java.lang.annotation.Annotation;

@Component
public class WebSocketControllerAnnotationHandler extends AnnotationHandler<WebSockerController> {


    @Override
    public Class<WebSockerController> getAnnotationType() {
        return WebSockerController.class;
    }

    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {
        String path = ((WebSockerController) annotation).value();
        DispatcherHandler.webSocketFrameHandlerMap.put(path, beanDefinition.getBean());
    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }
}
