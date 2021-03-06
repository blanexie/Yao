package xyz.xiezc.ioc.starter.annotation;

import xyz.xiezc.ioc.starter.annotation.handler.ConfigurationAnnotationHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置类注解,
 * 可以继承@Component注解的
 *
 * @author wb-xzc291800
 * @date 2019/04/08 16:09
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {

    String value() default "";

}
