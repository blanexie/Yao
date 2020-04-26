package xyz.xiezc.ioc.common.create.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.BeanCreateUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.util.Set;

public class FactoryBeanCreateStrategy implements BeanCreateStrategy {

    Log log = LogFactory.get(SimpleBeanCreateStategy.class);

    BeanCreateUtil beanCreateUtil;

    @Override
    public void createBean(BeanDefinition beanDefinition) {
        BeanStatusEnum beanStatus = beanDefinition.getBeanStatus();
        if (beanStatus == BeanStatusEnum.Completed) {
            log.info("beanDefinition已经初始化了， BeanDefinition:{}", beanDefinition.toString());
            return;
        }
        //检查所有需要注入字段的依赖是否都存在
        Set<FieldDefinition> annotationFiledDefinitions = beanDefinition.getAnnotationFiledDefinitions();
        beanCreateUtil.checkFieldDefinitions(annotationFiledDefinitions);
        //检查@Init注解的方法的参数是否都是空的， bean的init方法的参数必须是空的
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        beanCreateUtil.checkInitMethod(initMethodDefinition);

        //先创建类
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.HalfCooked) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
            beanDefinition.setBean(bean);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }
        //注入字段
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.HalfCooked) {
            //设置字段的属性值
            beanCreateUtil.injectFieldValue(beanDefinition);
            beanDefinition.setBeanStatus(BeanStatusEnum.injectField);
        }
        //调用init方法
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.injectField) {
            beanCreateUtil.doInitMethod(beanDefinition);
            //bean 所有对象设置完整
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        }
    }

    @Override
    public BeanTypeEnum getBeanTypeEnum() {
        return BeanTypeEnum.factoryBean;
    }

    @Override
    public void setBeanCreateUtil(BeanCreateUtil beanCreateUtil) {
        this.beanCreateUtil = beanCreateUtil;
    }
}