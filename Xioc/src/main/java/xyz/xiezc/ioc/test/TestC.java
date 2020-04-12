package xyz.xiezc.ioc.test;

import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;

@Component
public class TestC {

    @Inject
    TestA testA;

    @Inject
    TestB testB;


    public void print() {
        System.out.println("testC");
        testA.print();
        testB.print();
    }
}
