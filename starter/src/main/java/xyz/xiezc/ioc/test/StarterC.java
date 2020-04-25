package xyz.xiezc.ioc.test;


import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.starter.StarterApplication;

@Component
public class StarterC {

    @Inject
    StarterApplication StarterAp;

    public void print() {
        System.out.println("StarterC StarterCStarterCStarterC");
    }


}
