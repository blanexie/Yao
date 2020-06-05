package xyz.xiezc.ioc.starter.example;


import lombok.Data;
import xyz.xiezc.ioc.starter.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.annotation.Configuration;
import xyz.xiezc.ioc.starter.annotation.Cron;
import xyz.xiezc.ioc.starter.annotation.EnableCron;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;

import java.time.LocalTime;

@Data
@EnableCron
@Configuration
public class Demo {

    @Cron("*/5 * * * * *")
    public void test() {
        System.out.println(LocalTime.now());
    }

    private Integer s = 10;

    public static void main(String[] args) {
        ApplicationContextUtil run = Xioc.run(Demo.class);
        BeanDefinition beanDefinition = run.getBeanDefinitionContext().getBeanDefinition(Demo.class);
        Object bean = beanDefinition.getBean();
        System.out.println(((Demo) bean).s);
    }
}
