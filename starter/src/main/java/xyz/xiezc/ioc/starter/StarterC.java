package xyz.xiezc.ioc.starter;


import xyz.xiezc.ioc.annotation.Component;


@Component
public class StarterC {

    public void print() {
        System.out.println("这是一个StarterC类的对象，" + this);
    }

}
