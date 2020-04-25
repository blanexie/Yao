package xyz.xiezc.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入依赖
 * <p>
 * name 一样， class 一样 ：  注入
 * name 不一样， class 一样： 注入
 * name 不一样，  class有子bean ： 选择第一个子bean注入
 * name 一样， class有子bean： 选择子bean中name一样的注入
 * name 不一样， class无子bean： 抱错
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:19
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {

    /**
     * @return
     */
    String value() default "";

}
