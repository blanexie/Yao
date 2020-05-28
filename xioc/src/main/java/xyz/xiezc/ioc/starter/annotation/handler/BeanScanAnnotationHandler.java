package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.annotation.*;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.system.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BeanScanAnnotationHandler extends AnnotationHandler<BeanScan> {

    //最小的值
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public Class getAnnotationType() {
        return BeanScan.class;
    }

    @Inject
    ApplicationContextUtil applicationContextUtil;

    @Override
    public void processClass(BeanScan annotation, Class clazz) {
        //校验是否在@Configuration注解的bean内部
        if (AnnotationUtil.getAnnotation(clazz, Configuration.class) == null) {
            ExceptionUtil.wrapAndThrow(new RuntimeException("@BeanScan必须在@Configuration注解的类上使用，error class:" + clazz.getName()));
        }

        Class<?>[] classes = annotation.basePackageClasses();
        List<String> packages = CollUtil.newArrayList(classes)
                .stream()
                .map(ClassUtil::getPackage)
                .collect(Collectors.toList());
        String[] strings = annotation.basePackages();
        for (String string : strings) {
            packages.add(string);
        }
        for (String aPackage : packages) {
            applicationContextUtil.loadBeanDefinitions(aPackage);
        }
    }


    @Override
    public void processMethod(MethodDefinition method, BeanScan annotation, BeanDefinition beanSignature) {

    }

    @Override
    public void processField(FieldDefinition field, BeanScan annotation, BeanDefinition beanSignature) {

    }
}
