package xyz.xiezc.ioc.starter.web.demo;

import xyz.xiezc.ioc.starter.example.Yao;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;

public class YaoWeb {
    public static void main(String[] args) {
        Yao.run(DispatcherHandler.class);
    }
}
