package xyz.xiezc.ioc.starter.core;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.setting.Setting;
import cn.hutool.setting.SettingUtil;
import xyz.xiezc.ioc.starter.core.context.AbstractApplicationContext;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.context.DefaultApplicationContext;

import java.net.URL;
import java.util.List;

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
        // 加载配置信息
        loadProperties(defaultApplicationContext);
        //loadBeanDefinition
        loadBeanDefinition(clazz, defaultApplicationContext);
        //调用所有的 BeanFactoryPostProcess
        invokeBeanFactoryPostProcess(defaultApplicationContext);
        //初始化加载所有的事件监听器
        loadListener(defaultApplicationContext);


    }

    private static void loadListener(AbstractApplicationContext abstractApplicationContext) {


    }

    private static void invokeBeanFactoryPostProcess(AbstractApplicationContext abstractApplicationContext) {
        abstractApplicationContext.invokeBeanFactoryPostProcess();
    }

    private static void loadProperties(AbstractApplicationContext abstractApplicationContext) {
        URL resource = ResourceUtil.getResource("yao.properties");
        abstractApplicationContext.setSetting(new Setting(resource, CharsetUtil.CHARSET_UTF_8, true));
    }


    private static void loadBeanDefinition(Class[] clazz, AbstractApplicationContext abstractApplicationContext) {
        for (Class aClass : clazz) {
            abstractApplicationContext.loadBeanDefinitions(aClass.getPackageName());
        }
    }
}