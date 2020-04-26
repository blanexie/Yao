package xyz.xiezc.ioc.starter;


import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Bean;
import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.test.StarterA;
import xyz.xiezc.ioc.test.StarterC;

import java.util.Map;

@BeanScan(basePackageClasses = {StarterC.class})
@Configuration
public class StarterApplication {

    @Inject
    StarterC starterC;

    public static void main(String[] args) {
        Xioc.getSingleton().run(StarterApplication.class);

        BeanDefinition beanDefinition = Xioc.getSingleton().getContextUtil().getBeanDefinition(StarterA.class);

        System.out.println(beanDefinition.toString());
    }

}
