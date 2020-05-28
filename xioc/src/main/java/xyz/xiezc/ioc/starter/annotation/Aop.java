package xyz.xiezc.ioc.starter.annotation;

import xyz.xiezc.ioc.starter.annotation.handler.AopAnnotationHandler;
import xyz.xiezc.ioc.system.common.AopAspect;
import xyz.xiezc.ioc.system.annotation.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Aop {

    /**
     * 切面类
     *
     * @return
     */
    Class<? extends AopAspect> value();

    /**
     * 注解处理器
     */
    Class<? extends AnnotationHandler> annotatonHandler() default AopAnnotationHandler.class;

}
