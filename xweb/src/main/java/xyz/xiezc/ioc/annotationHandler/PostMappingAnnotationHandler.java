package xyz.xiezc.ioc.annotationHandler;

import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.starter.WebConfiguration;
import xyz.xiezc.web.annotation.PostMapping;
import xyz.xiezc.web.common.MappingHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PostMappingAnnotationHandler extends AnnotationHandler<PostMapping> {

    @Override
    public Class<PostMapping> getAnnotationType() {
        return PostMapping.class;
    }

    @Override
    public void processClass(PostMapping annotation, Class clazz, ContextUtil contextUtil) {

    }

    @Override
    public void processMethod(Method method, PostMapping annotation, BeanSignature beanSignature, ContextUtil contextUtil) {
        //获取controller
        dealMappingMethod(method, beanSignature, contextUtil, annotation.value());
    }

    static void dealMappingMethod(Method method, BeanSignature beanSignature, ContextUtil contextUtil, String value) {
        BeanDefinition controller = contextUtil.getBeanDefinitionBySignature(beanSignature);
        if (controller == null) {
            throw new RuntimeException("请在Controller中使用PostMapping注解， method：" + method.getName());
        }

        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        String beanName = controller.getBeanName();
        if (!beanName.startsWith("/")) {
            beanName = "/" + beanName;
        }
        String path = beanName + value;

        MappingHandler mappingHandler = new MappingHandler();
        mappingHandler.setPath(path);
        mappingHandler.setBeanDefinition(controller);
        mappingHandler.setMethod(method);
        WebConfiguration.mappingHandlerMap.put(path, mappingHandler);
    }

    @Override
    public void processField(Field field, PostMapping annotation, BeanSignature beanSignature, ContextUtil contextUtil) {

    }
}
