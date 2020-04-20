package xyz.xiezc.ioc.starter.annotationHandler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Bean;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.common.XiocUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * 被configuration 注解的方法的处理逻辑
 */
public class ConfigurationAnnotationHandler extends AnnotationHandler<Configuration> {

    @Override
    public Class<Configuration> getAnnotationType() {
        return Configuration.class;
    }

    @Override
    public void processClass(Configuration annotation, Class clazz, ContextUtil contextUtil) {
        BeanDefinition beanDefinition = XiocUtil.dealBeanAnnotation(annotation, clazz, contextUtil);
        contextUtil.addBeanDefinition(beanDefinition);
    }


    @Override
    public void processMethod(MethodDefinition method, Configuration annotation, BeanDefinition beanSignature, ContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition field, Configuration annotation, BeanDefinition beanSignature, ContextUtil contextUtil) {

    }


}
