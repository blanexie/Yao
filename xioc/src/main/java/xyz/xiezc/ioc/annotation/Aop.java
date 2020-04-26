package xyz.xiezc.ioc.annotation;

import cn.hutool.aop.aspects.Aspect;
import xyz.xiezc.ioc.annotation.handler.AopAnnotationHandler;
import xyz.xiezc.ioc.annotation.handler.InitAnnotationHandler;
import xyz.xiezc.ioc.common.AopAspect;

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
    Class<? extends AnnotationHandler> annotatonHandler = AopAnnotationHandler.class;

}
