package xyz.xiezc.ioc.starter.annotation.aop;

import cn.hutool.aop.aspects.Aspect;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.aop.XAspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Aop {

  /**
   * 切面类
   *
   * @return
   */
  Class<? extends XAspect> value();


}
