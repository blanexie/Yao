package xyz.xiezc.ioc.annotationHandler;

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
        //处理内部的component注解
        Method[] methods = ReflectUtil.getMethods(clazz);
        for (Method method : methods) {
            Bean bean = AnnotationUtil.getAnnotation(method, Bean.class);
            if (bean == null) {
                continue;
            }
            Class<?> returnType = method.getReturnType();

            BeanDefinition methodBeanDefinition = XiocUtil.dealBeanAnnotation(bean, returnType, contextUtil);
            //重新 设置beanName
            String beanName = bean.value();
            if (StrUtil.isBlank(beanName)) {
                beanName = method.getName();
            }
            //
            methodBeanDefinition.setBeanName(beanName);
            methodBeanDefinition.setParentBeanDefinition(beanDefinition);
            methodBeanDefinition.setMethod(method);

            contextUtil.addBeanDefinition(methodBeanDefinition);
        }
    }


    @Override
    public void processMethod(Method method, Configuration annotation, BeanSignature beanSignature, ContextUtil contextUtil) {

    }

    @Override
    public void processField(Field field, Configuration annotation, BeanSignature beanSignature, ContextUtil contextUtil) {

    }


}
