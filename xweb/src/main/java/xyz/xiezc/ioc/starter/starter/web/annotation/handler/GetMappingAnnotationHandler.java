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
import xyz.xiezc.ioc.starter.starter.web.annotation.GetMapping;

import java.lang.reflect.AnnotatedElement;

@Component
public class GetMappingAnnotationHandler extends AnnotationHandler<GetMapping> {

    @Override
    public Class<GetMapping> getAnnotationType() {
        return GetMapping.class;
    }

    @Override
    public void processClass(GetMapping annotation, Class clazz, ApplicationContextUtil contextUtil) {

        


    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, GetMapping annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {
        //获取controller配置的路径
        AnnotatedElement annotatedElement = beanDefinition.getAnnotatedElement();
        Controller controller = AnnotationUtil.getAnnotation(annotatedElement, Controller.class);
        String controllerMapping = controller.value();
        String methodReqMapping = annotation.value();
        String path = StrUtil.join("/", controllerMapping, methodReqMapping);
        path = "/" + path;
        DispatcherHandler.getMethods.put(FileUtil.normalize(path), methodDefinition);



    }

    @Override
    public void processField(FieldDefinition fieldDefinition, GetMapping annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
