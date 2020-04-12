package xyz.xiezc.starter;


import xyz.xiezc.example.TestExample;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

public class StarterApplication {


    public static void main(String[] args) {
        Xioc xioc = Xioc.run(StarterApplication.class);
        BeanSignature beanSignature = new BeanSignature();
        beanSignature.setBeanClass(StarterC.class);
        beanSignature.setBeanTypeEnum(BeanTypeEnum.bean);
        beanSignature.setBeanName("testc");

        BeanDefinition beanDefinition = xioc.getContextUtil().getComplatedBeanDefinitionBySignature(beanSignature);
        StarterC bean = beanDefinition.getBean();
        bean.print();

    }
}
