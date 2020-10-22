package xyz.xiezc.ioc.starter.core;

import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.context.DefaultApplicationContext;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 11:26 上午
 **/
public class Yao {


    public static ApplicationContext run(Class... clazz) {
        //创建容器
        DefaultApplicationContext defaultApplicationContext = new DefaultApplicationContext();
        //loadBeanDefinition
        for (Class aClass : clazz) {
            defaultApplicationContext.loadBeanDefinitions(aClass.getPackageName());
        }

        //调用所有的 BeanFactoryPostProcess
        defaultApplicationContext.invokeBeanFactoryPostProcess();



    }
}