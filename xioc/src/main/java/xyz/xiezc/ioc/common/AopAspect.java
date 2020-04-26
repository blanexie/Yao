package xyz.xiezc.ioc.common;

import cn.hutool.aop.aspects.Aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class AopAspect implements Aspect {

    /**
     * 要拦截的方法
     */
    List<Method> methods = new ArrayList<>();

    /**
     * 新增这个切面类需要拦截的方法
     */
    public void addMethod(Method method) {
        methods.add(method);
    }

    /**
     * 目标方法执行前的操作
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args   参数
     * @return 是否继续执行接下来的操作
     */
    @Override
    public boolean before(Object target, Method method, Object[] args) {
        if (methods.contains(method)) {
            return doBefore(target, method, args);
        }
        return true;
    }

    public abstract boolean doBefore(Object target, Method method, Object[] args);

    /**
     * 目标方法执行后的操作
     * 如果 target.method 抛出异常且
     *
     * @param target    目标对象
     * @param method    目标方法
     * @param args      参数
     * @param returnVal 目标方法执行返回值
     * @return 是否允许返回值（接下来的操作）
     * @see Aspect#afterException 返回true,则不会执行此操作
     * 如果
     * @see Aspect#afterException 返回false,则无论target.method是否抛出异常，均会执行此操作
     */
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        if (methods.contains(method)) {
            return doAfter(target, method, args, returnVal);
        }
        return true;
    }

    public abstract boolean doAfter(Object target, Method method, Object[] args, Object returnVal);

    /**
     * 目标方法抛出异常时的操作
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args   参数
     * @param e      异常
     * @return 是否允许抛出异常
     */
    public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
        if (methods.contains(method)) {
            return doAfter(target, method, args, e);
        }
        return true;
    }

    public abstract boolean doAfterException(Object target, Method method, Object[] args, Throwable e);
}
