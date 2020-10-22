package xyz.xiezc.ioc.starter.annotation.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 放入容器
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

    /**
     * 放入容器的类名
     *
     * @return
     */
    String value() default "";

}
