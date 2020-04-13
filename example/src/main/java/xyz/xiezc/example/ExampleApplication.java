package xyz.xiezc.example;


import xyz.xiezc.example.web.TestController;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.WebConfiguration;
import xyz.xiezc.ioc.test.StarterC;


public class ExampleApplication {

    public static void main(String[] args) {
        Xioc xioc = Xioc.run(ExampleApplication.class);
        BeanSignature beanSignature = new BeanSignature();
        beanSignature.setBeanClass(WebConfiguration.class);
        beanSignature.setBeanTypeEnum(BeanTypeEnum.bean);
        beanSignature.setBeanName("testc");

        BeanDefinition beanDefinition = xioc.getContextUtil().getComplatedBeanDefinitionBySignature(beanSignature);
        WebConfiguration bean = beanDefinition.getBean();


        beanSignature.setBeanClass(TestController.class);
        BeanDefinition beanDefinition1 = xioc.getContextUtil().getComplatedBeanDefinitionBySignature(beanSignature);
        WebConfiguration bean1 = beanDefinition.getBean();

        System.out.println();
    }


}
