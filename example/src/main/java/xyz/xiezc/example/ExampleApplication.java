package xyz.xiezc.example;


import xyz.xiezc.example.web.TestController;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;

@MapperScan("xyz.xiezc.example.web")
@Component
public class ExampleApplication {

    public static void main(String[] args) {
        Xioc xioc = Xioc.run(ExampleApplication.class);
        BeanDefinition injectBeanDefinition = xioc.getApplicationContextUtil().getBeanDefinition(TestController.class);
        TestController bean = injectBeanDefinition.getBean();

    }
}
