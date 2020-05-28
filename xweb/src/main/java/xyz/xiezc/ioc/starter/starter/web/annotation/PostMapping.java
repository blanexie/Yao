package xyz.xiezc.ioc.starter.starter.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 目前只支持请求和响应的content-type如下的值
 * application/json; charset=utf-8
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PostMapping {
    String value();
}
