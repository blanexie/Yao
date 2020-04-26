package xyz.xiezc.ioc.test;

import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.common.AopAspect;

import java.lang.reflect.Method;

@Component
public class StartAspect extends AopAspect {
    @Override
    public boolean doBefore(Object target, Method method, Object[] args) {
        System.out.println(method.getName() + " = doBefore");
        return true;
    }

    @Override
    public boolean doAfter(Object target, Method method, Object[] args, Object returnVal) {
        System.out.println(method.getName() + " = doAfter");
        return true;
    }

    @Override
    public boolean doAfterException(Object target, Method method, Object[] args, Throwable e) {
        System.out.println(method.getName() + " = doAfterException");
        return true;
    }
}
