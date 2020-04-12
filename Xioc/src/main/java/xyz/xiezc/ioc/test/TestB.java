package xyz.xiezc.ioc.test;

import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;

public class TestB {

    @Inject
    TestA testA;

    @Value("xyz.xiezc.t")
    Integer xx;

    public void print() {
        System.out.println("this is testB,  value: " + xx);
    }

}
