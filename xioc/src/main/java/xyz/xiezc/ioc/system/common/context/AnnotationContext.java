package xyz.xiezc.ioc.system.common.context;

import xyz.xiezc.ioc.system.annotation.AnnotationHandler;

import java.lang.annotation.Annotation;

public interface AnnotationContext {

    /**
     * 添加一个注解处理器
     *
     * @param annotationHandler
     * @param <T>
     */
    <T extends Annotation> void addAnnotationHandler(AnnotationHandler<T> annotationHandler);

    /**
     * 添加一个注解处理器
     *
     * @param clazz
     */
    void addNotHandleAnnotation(Class<? extends Annotation> clazz);

    /**
     * 判断是否是不需要注解处理器处理的注解
     *
     * @param clazz
     * @return
     */
    boolean isNotHandleAnnotation(Class<? extends Annotation> clazz);

    /**
     * 获取一个Class上面的注解处理器
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends Annotation> AnnotationHandler<T> getClassAnnotationHandler(Class<T> clazz);

    /**
     * 获取一个Method上面的注解处理器
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends Annotation> AnnotationHandler<T> getMethodAnnotationHandler(Class<T> clazz);

    /**
     * 获取一个Field上面的注解处理器
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends Annotation> AnnotationHandler<T> getFieldAnnotationHandler(Class<T> clazz);

    /**
     * 获取一个注解上面的注解处理器
     * 注解的注解的处理器
     *
     * @param <T>
     * @param clazz
     * @return
     */
    <T extends Annotation> AnnotationHandler<T> getAnnotationAnnotationHandler(Class<?> clazz);

    /**
     * 获取一个方法参数 parameter上面的注解处理器
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends Annotation> AnnotationHandler<T> getParameterAnnotationHandler(Class<T> clazz);


}
