package xyz.xiezc.ioc.example;

import xyz.xiezc.ioc.starter.annotation.aop.Aop;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.aop.TimeIntervalAspect;

@Aop(TimeIntervalAspect.class)
@Component
public class B {

    public void print() {
        System.out.println("print B");
        System.out.println("--------");
    }


    public void print2() {
        System.out.println("print B2");
        System.out.println("--------");
    }
}
