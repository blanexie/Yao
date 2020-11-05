package xyz.xiezc.ioc.starter;

import xyz.xiezc.ioc.starter.annotation.core.Component;

@Component
public class C {

    public void print() {
        System.out.println("print B");
        System.out.println("--------");

    }
}
