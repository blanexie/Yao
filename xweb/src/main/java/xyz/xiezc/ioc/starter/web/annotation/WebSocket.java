package xyz.xiezc.ioc.starter.web.annotation;

import xyz.xiezc.ioc.annotation.core.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface WebSocket {

    String value();
}
