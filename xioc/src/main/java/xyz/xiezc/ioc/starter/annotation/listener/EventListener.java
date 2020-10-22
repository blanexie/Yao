package xyz.xiezc.ioc.starter.annotation.listener;


import xyz.xiezc.ioc.starter.annotation.core.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件处理器的注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface EventListener {

    /**
     * 被注解的事件处理器需要处理的事件名称
     * @return
     */
    String[] eventName();
}
