package xyz.xiezc.ioc.system.example;


import lombok.Data;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.system.Xioc;
import xyz.xiezc.ioc.system.annotation.Configuration;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;

@Data
@Configuration
public class Demo {


    private Integer s = 10;

    public static void main(String[] args) {
        ApplicationContextUtil run = Xioc.run(Demo.class);
        BeanDefinition beanDefinition = run.getBeanDefinitionContext().getBeanDefinition(Demo.class);

        Demo bean = beanDefinition.getBean();
        System.out.println(bean.getS());
    }
}
