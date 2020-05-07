package xyz.xiezc.ioc.starter.orm.annotation;

import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.handler.BeanAnnotationHandler;
import xyz.xiezc.ioc.starter.orm.annotation.handler.MapperScanAnnotationHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解标示 mapper接口的位置. 默认mapper.xml和接口是在同一位置
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MapperScan {

    String[] value() ;

    /**
     * 注解处理器
     */
    Class<? extends AnnotationHandler> annotationHandler= MapperScanAnnotationHandler.class;

}
