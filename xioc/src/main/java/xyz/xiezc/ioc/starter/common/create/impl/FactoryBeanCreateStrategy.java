package xyz.xiezc.ioc.starter.common.create.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import xyz.xiezc.ioc.starter.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.common.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;

import java.util.Set;


public class FactoryBeanCreateStrategy extends BeanCreateStrategy {

    Log log = LogFactory.get(SimpleBeanCreateStategy.class);

    @Override
    public BeanTypeEnum getBeanTypeEnum() {
        return BeanTypeEnum.factoryBean;
    }

    @Override
    public BeanDefinition createBean(BeanDefinition beanDefinition) {
        BeanStatusEnum beanStatus = beanDefinition.getBeanStatus();
        if (beanStatus != BeanStatusEnum.Original) {
            return beanDefinition;
        }

        //检查所有需要注入字段的依赖是否都存在
        Set<FieldDefinition> fieldDefinitions = beanDefinition.getFieldDefinitions();
        this.checkFieldDefinitions(fieldDefinitions);
        //检查@Init注解的方法的参数是否都是空的， bean的init方法的参数必须是空的
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        this.checkInitMethod(initMethodDefinition);

        //先创建类
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
            beanDefinition.setBean(bean);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }

        return beanDefinition;
    }


}
