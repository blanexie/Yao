package xyz.xiezc.ioc.starter.example;

import xyz.xiezc.ioc.starter.annotation.aop.Aop;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.aop.TimeIntervalAspect;


@Component
public class A {

    @Aop(TimeIntervalAspect.class)
    public void print() {
        System.out.println("print A");
        System.out.println("--------");
    }

    public void print2() {
        System.out.println("print A2");
        System.out.println("--------");
    }
}
