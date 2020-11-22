package xyz.xiezc.ioc.starter.example;

import xyz.xiezc.ioc.starter.annotation.aop.Aop;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.core.Init;
import xyz.xiezc.ioc.starter.aop.TimeIntervalAspect;

@Aop(TimeIntervalAspect.class)
@Component
public class B {

    @Init
    public void init() {
        System.out.println("print B init init init init init ");
    }

    public void print() {
        System.out.println("print B");
        System.out.println("--------");
    }


    public void print2() {
        System.out.println("print B2");
        System.out.println("--------");
    }
}
