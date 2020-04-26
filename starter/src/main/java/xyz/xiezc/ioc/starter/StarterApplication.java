package xyz.xiezc.ioc.starter;


import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.test.StarterA;
import xyz.xiezc.ioc.test.StarterC;

@BeanScan(basePackageClasses = {StarterC.class})
@Configuration
public class StarterApplication {

    @Inject
    StarterC starterC;

    public static void main(String[] args) {
        Xioc.getSingleton().run(StarterApplication.class);

        BeanDefinition beanDefinition = Xioc.getSingleton().getApplicationContextUtil().getBeanDefinition(StarterA.class);
        StarterA bean = beanDefinition.getBean();
        bean.print();
    }

}
