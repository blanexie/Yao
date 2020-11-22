package xyz.xiezc.ioc.starter.aop;

import xyz.xiezc.ioc.starter.annotation.core.Component;

import java.lang.reflect.Method;
import java.util.Date;

@Component
public class TimeIntervalAspect extends XAspect {


    @Override
    public boolean beforeMethod(Object target, Method method, Object[] args) {
        System.out.println("beforeMethod:" + new Date());
        return true;
    }

    @Override
    public boolean afterMethod(Object target, Method method, Object[] args) {
        System.out.println("afterMethod:" + new Date());
        return true;
    }

    @Override
    public boolean afterMethodException(Object target, Method method, Object[] args) {
        System.out.println("afterMethodException:" + new Date());
        return true;
    }
}
