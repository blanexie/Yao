package xyz.xiezc.ioc.system.common.context.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
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
import xyz.xiezc.ioc.system.common.exception.CircularDependenceException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
public class BeanCreateContextUtil implements BeanCreateContext {

    static Log log = LogFactory.get(BeanCreateContextUtil.class);

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


    /**
     * 校验循环依赖的set集合
     */
    static Set<BeanDefinition> creatingBeanDefinitions = new HashSet<>();


    /**
     * beanDefinition的初始化， 其初始化的过程较为复杂， 具体我分以下几步
     * 1. 将这个bean放入待初始化集合中，这个主要是用来解决循环依赖的判断的
     * 2. 获取这个bean中的用户自定义注解
     * 2. 校验这个bean的所有依赖在不在容器中， 包括配置文件中配置值（@Value）的依赖
     * 3. 检查有无自定义注解，如果有的话，此时获取对应的annotationHandler来处理下
     * 4. 开始初始化，并且注入字段值，修改对应状态
     * 5. 判断有无切面，有切面的话，生成代理类替换生成的bean
     *
     * @param beanDefinition
     * @return
     */
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

        try {
            //判断循环依赖， 同时把BeanDefinition 放路创建中的缓存map中
            if (isCircularDependenceBeanDefinition(beanDefinition)) {
                throw new CircularDependenceException(beanDefinition.toString());
            }
            return beanCreateStrategy.createBean(beanDefinition);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            //移除创建中的
            removeCreatingBeanDefinition(beanDefinition);
        }

    }


    /**
     * 移除正在创建中的bean
     *
     * @param beanDefinition
     */
    public void removeCreatingBeanDefinition(BeanDefinition beanDefinition) {
        creatingBeanDefinitions.remove(beanDefinition.getBeanName());
    }

    public boolean isCircularDependenceBeanDefinition(BeanDefinition beanDefinition) {
        return !creatingBeanDefinitions.add(beanDefinition);
    }
}
