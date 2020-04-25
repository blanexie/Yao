package xyz.xiezc.ioc.annotation;

import xyz.xiezc.ioc.annotation.handler.BeanAnnotationHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

    /**
     * 放入容器的类名
     *
     * @return
     */
    String value() default "";

    /**
     * 注解处理器
     */
    Class<? extends AnnotationHandler> annotationHandler= BeanAnnotationHandler.class;

}
