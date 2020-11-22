package xyz.xiezc.ioc.starter.annotation.cron;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定时任务的方法，  被这个注解标注的方法,会根据注解中配置的cron 表达式进行定时任务的调度
 * <p>
 * cron 表达式 可以参考这个 https://hutool.cn/docs/#/cron/%E5%85%A8%E5%B1%80%E5%AE%9A%E6%97%B6%E4%BB%BB%E5%8A%A1-CronUtil?id=%e4%bb%8b%e7%bb%8d
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cron {
    /**
     * cron 表达式
     *
     * @return
     */
    String value();
}
