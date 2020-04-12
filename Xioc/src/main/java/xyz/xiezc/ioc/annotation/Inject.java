package xyz.xiezc.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入依赖
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:19
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {

    String value() default "";

    /**
     * 如果是往集合中注入元素, 必须指定集合的类型,
     * 支持的集合有 Collection及其子类.
     * <p>
     * 不支持数组类型注入
     * <p>
     * 是按照类型注入, 且只是按照类型注入
     *
     * @return
     */
    Class<?> type() default void.class;




}
