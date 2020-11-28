package xyz.xiezc.ioc.starter.example;

import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.context.DefaultApplicationContext;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 11:26 上午
 **/

@Configuration
public class Yao {


    public static ApplicationContext run(Class clazz) {
        //创建容器
        DefaultApplicationContext defaultApplicationContext = new DefaultApplicationContext();
        return defaultApplicationContext.run(clazz);
    }

}