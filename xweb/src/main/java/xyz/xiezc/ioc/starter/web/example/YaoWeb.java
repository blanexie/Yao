package xyz.xiezc.ioc.starter.web.example;

import xyz.xiezc.ioc.starter.example.Yao;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;

/**
 * @author xiezc
 */
public class YaoWeb {
    public static void main(String[] args) {
        Yao.run(DispatcherHandler.class);
    }
}
