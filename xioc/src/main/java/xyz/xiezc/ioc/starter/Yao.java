package xyz.xiezc.ioc.starter;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.context.DefaultApplicationContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description  启动类
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 11:26 上午
 **/

@Configuration
public class Yao {


    private static final String startPackage = "xyz.xiezc.ioc.starter.config";

    public static ApplicationContext run(Class clazz) {
        //创建容器
        DefaultApplicationContext defaultApplicationContext = new DefaultApplicationContext();
        Set<Class<?>> classes = ClassUtil.scanPackage(startPackage);
        classes.stream()
                .filter(cla -> ClassUtil.isAssignable(ApplicationContext.class, cla))
                .map(cla -> {
                    ApplicationContext applicationContext = (ApplicationContext) ReflectUtil.newInstanceIfPossible(cla);
                    applicationContext.setApplicationContext(defaultApplicationContext);
                    return applicationContext;
                })
                .forEach(applicationContext -> {
                    defaultApplicationContext.setApplicationContext(applicationContext);
                });
        return defaultApplicationContext.run(clazz);
    }

}