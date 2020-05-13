package xyz.xiezc.ioc.starter.web.annotation.handler;

import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.WebSockerController;

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
