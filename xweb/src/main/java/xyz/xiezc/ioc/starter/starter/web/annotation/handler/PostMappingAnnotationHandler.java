package xyz.xiezc.ioc.starter.starter.web.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.starter.web.annotation.PostMapping;

import java.lang.annotation.Annotation;


@Component
public class PostMappingAnnotationHandler extends AnnotationHandler<PostMapping> {

    @Override
    public Class<PostMapping> getAnnotationType() {
        return PostMapping.class;
    }

    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {
        //获取controller配置的路径
        Controller controller = AnnotationUtil.getAnnotation(beanDefinition.getBeanClass(), Controller.class);
        String controllerMapping = controller.value();
        String methodReqMapping = ((PostMapping) annotation).value();
        String path = StrUtil.join("/", controllerMapping, methodReqMapping);
        DispatcherHandler.postMethods.put(FileUtil.normalize("/" + path), methodDefinition);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }
}
