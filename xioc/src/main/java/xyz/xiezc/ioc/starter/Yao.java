package xyz.xiezc.ioc.starter;

import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.context.DefaultApplicationContext;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 11:26 上午
 **/
public class Yao {


    public static ApplicationContext run(Class clazz) {
        //创建容器
        DefaultApplicationContext defaultApplicationContext = new DefaultApplicationContext();
        return defaultApplicationContext.run(clazz);
    }

    public static void main(String[] args) {
        ApplicationContext run = Yao.run(Yao.class);
        A a = run.getBean(A.class);
        B b = run.getBean(B.class);
        C c = run.getBean(C.class);
        a.print();
        b.print();
        c.print();

    }


}