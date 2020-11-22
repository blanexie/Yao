package xyz.xiezc.ioc.starter.example;

import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.cron.Cron;

@Component
public class C {

    @Cron("*/2 * * * * *")
    public void print() {
        System.out.println("print C");
        System.out.println("--------");

    }
}
