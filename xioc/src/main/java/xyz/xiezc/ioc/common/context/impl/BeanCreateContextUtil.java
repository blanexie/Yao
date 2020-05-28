package xyz.xiezc.ioc.common.context.impl;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.common.context.BeanCreateContext;
import xyz.xiezc.ioc.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.common.context.PropertiesContext;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class BeanCreateContextUtil implements BeanCreateContext {

    protected BeanDefinitionContext beanDefinitionContext;

    protected PropertiesContext propertiesContext;


    public BeanCreateContextUtil(BeanDefinitionContext beanDefinitionContext,  PropertiesContext propertiesContext) {
        this.beanDefinitionContext = beanDefinitionContext;
        this.propertiesContext = propertiesContext;
    }

    /**
     *
     */
    Map<BeanTypeEnum, BeanCreateStrategy> beanCreateStrategyMap = new HashMap<>();

    @Override
    public BeanDefinition createBean(BeanDefinition beanDefinition) {

        return null;
    }

}
