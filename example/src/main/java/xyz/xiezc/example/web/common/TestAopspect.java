package xyz.xiezc.example.web.common;

import cn.hutool.json.JSONUtil;
import xyz.xiezc.ioc.starter.annotation.Component;
import xyz.xiezc.ioc.starter.common.AopAspect;

import java.lang.reflect.Method;

/**
 * @Description 测试切面
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/4 10:13 上午
 **/
@Component
public class TestAopspect extends AopAspect {
    @Override
    public boolean doBefore(Object target, Method method, Object[] args) {
        System.out.println("TestAopspect doBefore " + method.getName() + " args:" + JSONUtil.toJsonStr(args));
        return true;
    }

    @Override
    public boolean doAfter(Object target, Method method, Object[] args, Object returnVal) {
        System.out.println("TestAopspect doAfter " + method.getName() + " args:" + JSONUtil.toJsonStr(args) + " returnVal:" + JSONUtil.toJsonStr(returnVal));
        return true;
    }

    @Override
    public boolean doAfterException(Object target, Method method, Object[] args, Throwable e) {
        return true;
    }
}