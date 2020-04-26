package xyz.xiezc.ioc.common.context.impl;


import cn.hutool.core.annotation.AnnotationUtil;
import lombok.Data;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.common.context.AnnotationContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 处理注解相关的工具类
 */
@Data
public class AnnotationContextUtil implements AnnotationContext {

    public static Set<Class<? extends Annotation>> excludeAnnotation = new HashSet<>() {{
        add(Override.class);
        add(Deprecated.class);
    }};

    /**
     * 作用于类上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> classAnnoAndHandlerMap = new HashMap<>();
    /**
     * 作用于方法上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> methodAnnoAndHandlerMap = new HashMap<>();
    /**
     * 作用于字段上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> fieldAnnoAndHandlerMap = new HashMap<>();

    /**
     * 作用于注解上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> annoAndHandlerMap = new HashMap<>();

    /**
     * 作用于参数上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> parameterAnnoAndHandlerMap = new HashMap<>();

    /**
     * 增加一个注解处理器
     *
     * @param annotationHandler
     */
    @Override
    public <T extends Annotation> void addAnnotationHandler(AnnotationHandler<T> annotationHandler) {
        //获取对应的注解

        Class<? extends Annotation> annotation = annotationHandler.getAnnotationType();
        ElementType[] value = AnnotationUtil.getTargetType(annotation);
        for (ElementType elementType : value) {
            //作用于类上
            if (elementType == ElementType.TYPE) {
                this.classAnnoAndHandlerMap.put(annotation, annotationHandler);
            }
            //作用于方法上面
            if (elementType == ElementType.METHOD) {
                this.methodAnnoAndHandlerMap.put(annotation, annotationHandler);
            }
            //作用于字段上面
            if (elementType == ElementType.FIELD) {
                this.fieldAnnoAndHandlerMap.put(annotation, annotationHandler);
            }

            //作用于参数上面
            if (elementType == ElementType.PARAMETER) {
                this.parameterAnnoAndHandlerMap.put(annotation, annotationHandler);
            }
            //作用于注解上面
            if (elementType == ElementType.PARAMETER) {
                this.annoAndHandlerMap.put(annotation, annotationHandler);
            }
        }
    }

    @Override
    public void addNotHandleAnnotation(Class<? extends Annotation> clazz) {
        excludeAnnotation.add(clazz);
    }

    @Override
    public boolean isNotHandleAnnotation(Class<? extends Annotation> clazz) {
        return excludeAnnotation.contains(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getClassAnnotationHandler(Class<T> clazz) {
        return classAnnoAndHandlerMap.get(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getMethodAnnotationHandler(Class<T> clazz) {
        return methodAnnoAndHandlerMap.get(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getFieldAnnotationHandler(Class<T> clazz) {
        return fieldAnnoAndHandlerMap.get(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getAnnotationAnnotationHandler(Class<T> clazz) {
        return annoAndHandlerMap.get(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getParameterAnnotationHandler(Class<T> clazz) {
        return parameterAnnoAndHandlerMap.get(clazz);
    }
}
