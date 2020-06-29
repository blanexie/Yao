package xyz.xiezc.ioc.starter.annotation;

import java.lang.annotation.*;

/**
 * 必须和Configuration注解配合使用才会生效，单独使用会报错
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyInject {

    /**
     * 会将配置文件中以这个配置为前缀的属性导入到对应的类中
     *
     * @return
     */
    String prefix();

}
