package xyz.xiezc.ioc.starter.core.process;

import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.core.BeanCreateUtil;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

/**
 * @Description 处理@Autowire和@Value注解
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 3:18 下午
 **/
@Component
public class AutowireAndValueBeanPostProcess implements BeanPostProcess {


    @Override
    public boolean beforeInstance(ApplicationContext applicationContext, BeanDefinition beanDefinition) {
        return true;
    }

    @Override
    public boolean beforeInit(ApplicationContext applicationContext, BeanDefinition beanDefinition) {
        BeanCreateUtil instacne = BeanCreateUtil.getInstacne();
        instacne.createAndInjectBean(beanDefinition, applicationContext);
        return true;
    }

    @Override
    public boolean afterInit(ApplicationContext applicationContext, BeanDefinition beanDefinition) {
        return true;
    }
}