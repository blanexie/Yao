package xyz.xiezc.ioc.starter;


import lombok.Data;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.definition.BeanDefinition;

@Data
@Configuration
public class StarterApplication {

    @Inject
    StarterC starterC;

    public static void main(String[] args) {
        Xioc xioc = Xioc.run(StarterApplication.class);
        BeanDefinition beanDefinition = xioc.getApplicationContextUtil().getBeanDefinition(StarterApplication.class);
        StarterApplication starterApplication = beanDefinition.getBean();
        StarterC starterC = starterApplication.getStarterC();
        starterC.print();
    }

}
