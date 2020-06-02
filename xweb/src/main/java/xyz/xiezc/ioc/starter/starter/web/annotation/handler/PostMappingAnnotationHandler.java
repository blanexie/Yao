package xyz.xiezc.ioc.starter.starter.web.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.system.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.starter.web.annotation.PostMapping;

import java.lang.reflect.AnnotatedElement;
@Component
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
        //获取controller配置的路径
        AnnotatedElement annotatedElement = beanDefinition.getAnnotatedElement();
        Controller controller = AnnotationUtil.getAnnotation(annotatedElement, Controller.class);
        String controllerMapping = controller.value();
        String methodReqMapping = annotation.value();
        String path = StrUtil.join("/", controllerMapping, methodReqMapping);
        DispatcherHandler.postMethods.put(FileUtil.normalize("/"+path), methodDefinition);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, PostMapping annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
