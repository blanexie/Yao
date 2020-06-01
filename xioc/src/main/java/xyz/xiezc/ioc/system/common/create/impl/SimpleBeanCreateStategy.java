package xyz.xiezc.ioc.system.common.create.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.system.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.system.common.exception.CircularDependenceException;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;

import java.util.Set;


@Data
public class SimpleBeanCreateStategy extends BeanCreateStrategy {

    Log log = LogFactory.get(SimpleBeanCreateStategy.class);

    @Override
    public BeanTypeEnum getBeanTypeEnum() {
        return BeanTypeEnum.bean;
    }

    /**
      *
     * @param beanDefinition
     * @return
     */
    @SneakyThrows
    @Override
    public BeanDefinition createBean(BeanDefinition beanDefinition) {
        BeanStatusEnum beanStatus = beanDefinition.getBeanStatus();
        if (beanStatus == BeanStatusEnum.Completed) {
            log.info("beanDefinition已经初始化了， BeanDefinition:{}", beanDefinition.toString());
            return beanDefinition;
        }


        //检查所有需要注入字段的依赖是否都存在
        Set<FieldDefinition> fieldDefinitions = beanDefinition.getFieldDefinitions();
        this.checkFieldDefinitions(fieldDefinitions);
        //检查init方法的参数是否都是空的， bean的init方法的参数必须是空的
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        this.checkInitMethod(initMethodDefinition);

        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
            beanDefinition.setBean(bean);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }

        //注入字段
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.HalfCooked) {
            //设置字段的属性值
            this.injectFieldValue(beanDefinition);
            beanDefinition.setBeanStatus(BeanStatusEnum.injectField);
        }

        //调用init方法
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.injectField) {
            this.doInitMethod(beanDefinition);
            //bean 所有对象设置完整
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        }


        return beanDefinition;
    }


}
