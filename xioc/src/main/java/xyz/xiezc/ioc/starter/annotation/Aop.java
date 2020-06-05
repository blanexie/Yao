package xyz.xiezc.ioc.starter.annotation;

import xyz.xiezc.ioc.starter.common.AopAspect;

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


}
