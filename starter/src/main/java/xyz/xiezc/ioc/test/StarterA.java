package xyz.xiezc.ioc.test;

import xyz.xiezc.ioc.annotation.Aop;
import xyz.xiezc.ioc.annotation.Component;
@Aop(StartAspect.class)
@Component
public class StarterA {

    public void print() {
        System.out.println("StarterAStarterAStarterAStarterAStarterA");
    }

}
