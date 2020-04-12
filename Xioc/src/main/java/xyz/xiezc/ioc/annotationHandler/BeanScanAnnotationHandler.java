package xyz.xiezc.ioc.annotationHandler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanSignature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class BeanScanAnnotationHandler extends AnnotationHandler<BeanScan> {

    @Override
    public Class getAnnotationType() {
        return BeanScan.class;
    }

    @Override
    public void processClass(BeanScan annotation, Class clazz, ContextUtil contextUtil) {
        Class<?>[] classes = annotation.basePackageClasses();
        List<String> packages = CollUtil.newArrayList(classes).stream()
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
    public void processMethod(Method method, BeanScan annotation, BeanSignature beanSignature, ContextUtil contextUtil) {

    }

    @Override
    public void processField(Field field, BeanScan annotation, BeanSignature beanSignature, ContextUtil contextUtil) {

    }
}
