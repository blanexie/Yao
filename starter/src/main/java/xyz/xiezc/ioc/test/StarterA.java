package xyz.xiezc.ioc.test;

import xyz.xiezc.ioc.annotation.Aop;
import xyz.xiezc.ioc.annotation.Component;

@Component
public class StarterA {

    @Aop(StartAspect.class)
    public void print() {
        System.out.println("StarterAStarterAStarterAStarterAStarterA");
    }

}
