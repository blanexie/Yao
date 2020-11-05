package xyz.xiezc.ioc.starter;

import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Component;

@Component
public class B {

    @Autowire
    A a;

    public void print() {
        System.out.println("print B");
        System.out.println("--------");
        a.print();
    }
}
