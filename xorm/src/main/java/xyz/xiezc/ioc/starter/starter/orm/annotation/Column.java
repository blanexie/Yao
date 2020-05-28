package xyz.xiezc.ioc.starter.starter.orm.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String value() default "";

    /**
     * 是否对应数据库中字段, 默认true 对应
     * @return
     */
    boolean exist() default true;

}
