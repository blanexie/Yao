package xyz.xiezc.ioc.starter;


import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Bean;
import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.test.StarterC;

import java.util.Map;

@BeanScan(basePackageClasses = {StarterC.class})
@Configuration
public class StarterApplication {

    @Inject
    StarterC starterC;

    @Bean
    public StarterC ss(){
        return new StarterC();
    }

    public static void main(String[] args) {
        Xioc.getSingleton().run(StarterApplication.class);

        Map<Class<?>, BeanDefinition> classaAndBeanDefinitionMap = Xioc.getSingleton().getContextUtil().getClassaAndBeanDefinitionMap();

        classaAndBeanDefinitionMap.forEach((clazz,beanDefinitiion)->{
            System.out.println(clazz.getName()+" : "+ beanDefinitiion.toString());
        });

    }

}
