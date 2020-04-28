package xyz.xiezc.ioc.starter;


import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.web.listener.WebServerListener;
import xyz.xiezc.ioc.starter.web.netty.NettyWebServerBootstrap;
import xyz.xiezc.ioc.test.StarterC;

@BeanScan(basePackageClasses = {StarterC.class})
@Configuration
public class StarterApplication {

    public static void main(String[] args) {
        Xioc xioc = Xioc.run(StarterApplication.class);

        BeanDefinition beanDefinition = xioc.getApplicationContextUtil().getBeanDefinition(StarterC.class);
        StarterC webServerListener = beanDefinition.getBean();
    }

}
