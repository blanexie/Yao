package xyz.xiezc.ioc.starter.aop;

import cn.hutool.aop.aspects.Aspect;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public abstract class XAspect implements Aspect {

    private Set<Method> aopMethods = new HashSet<>();

    public void addMethod(Method method) {
        aopMethods.add(method);
    }

    @Override
    public boolean before(Object target, Method method, Object[] args) {
        if (aopMethods.contains(method)) {
            return this.beforeMethod(target, method, args);
        }
        return true;
    }

    public abstract boolean beforeMethod(Object target, Method method, Object[] args);

    @Override
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        if (aopMethods.contains(method)) {
            return this.afterMethod(target, method, args);
        }
        return true;
    }

    public abstract boolean afterMethod(Object target, Method method, Object[] args);

    @Override
    public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
        if (aopMethods.contains(method)) {
            return this.afterMethodException(target, method, args);
        }
        return true;
    }

    public abstract boolean afterMethodException(Object target, Method method, Object[] args);

}
