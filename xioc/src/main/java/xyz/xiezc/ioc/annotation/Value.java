package xyz.xiezc.ioc.annotation;


import xyz.xiezc.ioc.annotation.handler.ValueAnnotationHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入配置
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:19
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {


    String value() default "";


    Class<? extends AnnotationHandler> annotatonHandler = ValueAnnotationHandler.class;
}
