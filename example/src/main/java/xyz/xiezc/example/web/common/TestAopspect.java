package xyz.xiezc.example.web.common;

import lombok.extern.slf4j.Slf4j;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.aop.XAspect;

import java.lang.reflect.Method;

/**
 * @Description 测试切面
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/4 10:13 上午
 **/
@Component
@Slf4j
public class TestAopspect extends XAspect {


    @Override
    public boolean beforeMethod(Object target, Method method, Object[] args) {
        log.info("TestAopspect beforeMethod， method:{}", method.getName());
        return true;
    }

    @Override
    public boolean afterMethod(Object target, Method method, Object[] args) {
        log.info("TestAopspect afterMethod, method:{}", method.getName());
        return true;
    }

    @Override
    public boolean afterMethodException(Object target, Method method, Object[] args) {
        log.info("TestAopspect afterMethodException, method:{}", method.getName());
        return true;
    }
}