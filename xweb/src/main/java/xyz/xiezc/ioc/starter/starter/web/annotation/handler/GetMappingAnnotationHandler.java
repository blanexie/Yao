package xyz.xiezc.ioc.starter.starter.web.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.annotation.Component;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.common.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.starter.web.annotation.GetMapping;

import java.lang.annotation.Annotation;

@Component
public class GetMappingAnnotationHandler extends AnnotationHandler<GetMapping> {

    @Override
    public Class<GetMapping> getAnnotationType() {
        return GetMapping.class;
    }

    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {
        //获取controller配置的路径
        Controller controller = AnnotationUtil.getAnnotation(beanDefinition.getBeanClass(), Controller.class);
        String controllerMapping = controller.value();
        String methodReqMapping = ((GetMapping) annotation).value();
        String path = StrUtil.join("/", controllerMapping, methodReqMapping);
        path = "/" + path;
        DispatcherHandler.getMethods.put(FileUtil.normalize(path), methodDefinition);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }
}
