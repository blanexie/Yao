package xyz.xiezc.ioc.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 系统注解， 有这个注解的类， 会在容器加载bean前使用反射创建对象， 所以这个类无法在注入容器中的bean
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemLoad {

}
