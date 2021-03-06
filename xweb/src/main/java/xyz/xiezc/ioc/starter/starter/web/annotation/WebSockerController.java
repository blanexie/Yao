package xyz.xiezc.ioc.starter.starter.web.annotation;

import xyz.xiezc.ioc.starter.annotation.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface WebSockerController {

    String value();
}
