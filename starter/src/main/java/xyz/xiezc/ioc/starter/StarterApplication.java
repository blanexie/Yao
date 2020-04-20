package xyz.xiezc.ioc.starter;


import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.test.StarterC;

@BeanScan(basePackageClasses = {StarterC.class})
@Component
public class StarterApplication {
    public static void main(String[] args) {
        Xioc.getSingleton().run(StarterApplication.class);
    }

}
