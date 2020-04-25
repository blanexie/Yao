package xyz.xiezc.ioc.starter.annotationHandler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public void processClass(BeanScan annotation, Class clazz, ContextUtil contextUtil) {
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
            Xioc.getSingleton().getBeanScanUtil().loadBeanDefinition(aPackage);
        }
    }


    @Override
    public void processMethod(MethodDefinition method, BeanScan annotation, BeanDefinition beanSignature, ContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition field, BeanScan annotation, BeanDefinition beanSignature, ContextUtil contextUtil) {

    }
}