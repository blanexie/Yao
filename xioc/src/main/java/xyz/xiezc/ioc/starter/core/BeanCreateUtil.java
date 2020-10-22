package xyz.xiezc.ioc.starter.core;

import cn.hutool.core.util.ReflectUtil;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;

import java.lang.reflect.Method;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 4:00 下午
 **/
public class BeanCreateUtil {


    public void createBean(BeanDefinition beanDefinition, ApplicationContext applicationContext) {
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
            if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.bean) {
                Object bean = ReflectUtil.newInstanceIfPossible(beanDefinition.getBeanClass());
                beanDefinition.setBean(bean);
                beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
            }

            if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.methodBean) {
                MethodDefinition invokeMethodBean = beanDefinition.getInvokeMethodBean();





                Object bean = ReflectUtil.newInstanceIfPossible(beanDefinition.getBeanClass());
                beanDefinition.setBean(bean);
                beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
            }

        }
    }


}