package xyz.xiezc.ioc.system.common.context.impl;

import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.system.common.context.BeanCreateContext;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.context.PropertiesContext;
import xyz.xiezc.ioc.system.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.system.common.create.impl.FactoryBeanCreateStrategy;
import xyz.xiezc.ioc.system.common.create.impl.MethodBeanCreateStrategy;
import xyz.xiezc.ioc.system.common.create.impl.SimpleBeanCreateStategy;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class BeanCreateContextUtil implements BeanCreateContext {

    protected BeanDefinitionContext beanDefinitionContext;

    protected PropertiesContext propertiesContext;

    public BeanCreateContextUtil(BeanDefinitionContext beanDefinitionContext, PropertiesContext propertiesContext) {
        this.beanDefinitionContext = beanDefinitionContext;
        this.propertiesContext = propertiesContext;
    }

    /**
     *
     */
    Map<BeanTypeEnum, BeanCreateStrategy> beanCreateStrategyMap = new HashMap<>();

    @Override
    public BeanDefinition createBean(BeanDefinition beanDefinition) {
        BeanTypeEnum beanTypeEnum = beanDefinition.getBeanTypeEnum();
        BeanCreateStrategy beanCreateStrategy = beanCreateStrategyMap.get(beanTypeEnum);
        if (beanCreateStrategy == null) {
            if (beanTypeEnum == BeanTypeEnum.bean) {
                beanCreateStrategy = new SimpleBeanCreateStategy();
            }
            if (beanTypeEnum == BeanTypeEnum.factoryBean) {
                beanCreateStrategy = new FactoryBeanCreateStrategy();
            }
            if (beanTypeEnum == BeanTypeEnum.methodBean) {
                beanCreateStrategy = new MethodBeanCreateStrategy();
            }
            beanCreateStrategy.setBeanDefinitionContext(beanDefinitionContext);
            beanCreateStrategy.setBeanCreateContext(this);
            beanCreateStrategy.setPropertiesContext(propertiesContext);
            beanCreateStrategyMap.put(beanTypeEnum, beanCreateStrategy);
        }

        return beanCreateStrategy.createBean(beanDefinition);
    }

}
