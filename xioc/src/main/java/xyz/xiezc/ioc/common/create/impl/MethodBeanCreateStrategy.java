package xyz.xiezc.ioc.common.create.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.common.context.BeanCreateContext;
import xyz.xiezc.ioc.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.common.context.PropertiesContext;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.exception.CircularDependenceException;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;
import xyz.xiezc.ioc.enums.FieldOrParamTypeEnum;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Component
public class MethodBeanCreateStrategy extends BeanCreateStrategy {

    Log log = LogFactory.get(SimpleBeanCreateStategy.class);


    @Override
    public BeanTypeEnum getBeanTypeEnum() {
        return BeanTypeEnum.methodBean;
    }


    @Override
    public BeanDefinition createBean(BeanDefinition beanDefinition) {
        BeanStatusEnum beanStatus = beanDefinition.getBeanStatus();
        if (beanStatus == BeanStatusEnum.Completed) {
            log.info("beanDefinition已经初始化了， BeanDefinition:{}", beanDefinition.toString());
            return beanDefinition;
        }
        //判断循环依赖， 同时把BeanDefinition 放路创建中的缓存map中
        if (isCircularDependenceBeanDefinition(beanDefinition)) {
            throw new CircularDependenceException(beanDefinition.toString());
        }

        //检查所有需要注入字段的依赖是否都存在
        Set<FieldDefinition> annotationFiledDefinitions = beanDefinition.getFieldDefinitions();
        this.checkFieldDefinitions(annotationFiledDefinitions);
        //检查@Init注解的方法的参数是否都是空的， bean的init方法的参数必须是空的
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        this.checkInitMethod(initMethodDefinition);
        //检查@Bean注解的方法的参数是否在容器中都存在
        MethodDefinition invokeMethodBean = beanDefinition.getInvokeMethodBean();
        this.checkMethodParam(invokeMethodBean.getParamDefinitions());

        //开始初始化
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
            //获取方法的调用方
            BeanDefinition beanDefinitionParent = invokeMethodBean.getBeanDefinition();
            //先初始化
            beanDefinitionParent = beanCreateContext.createBean(beanDefinitionParent);
            Object bean = beanDefinitionParent.getBean();
            //获取参数
            ParamDefinition[] paramDefinitions = invokeMethodBean.getParamDefinitions();
            for (ParamDefinition paramDefinition : paramDefinitions) {
                Object param = paramDefinition.getParam();
                if (param == null) {
                    dealParamDefinition(paramDefinition);
                } else {
                    if (param instanceof BeanDefinition) {
                        BeanDefinition beanDefinitionParam = beanCreateContext.createBean((BeanDefinition) param);
                        paramDefinition.setParam(beanDefinitionParam.getBean());
                    }
                }
            }
            Object[] objects = CollUtil.newArrayList(paramDefinitions).stream()
                    .map(paramDefinition ->
                            paramDefinition.getParam()
                    ).collect(Collectors.toList()).toArray();
            //调用方法， 获取结果
            Object invoke = ReflectUtil.invoke(bean, invokeMethodBean.getMethod(), objects);
            beanDefinition.setBean(invoke);
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

        removeCreatingBeanDefinition(beanDefinition);
        return beanDefinition;
    }

    private void dealParamDefinition(ParamDefinition paramDefinition) {
        if (FieldOrParamTypeEnum.Array == paramDefinition.getFieldOrParamTypeEnum()) {
            List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(paramDefinition.getParamType());
            List<Object> collect = beanDefinitions.stream().map(definition -> {
                beanCreateContext.createBean(definition);
                return definition.getBean();
            }).collect(Collectors.toList());
            paramDefinition.setParam(collect.toArray());
        }
        if (FieldOrParamTypeEnum.Collection == paramDefinition.getFieldOrParamTypeEnum()) {
            List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(paramDefinition.getParamType());
            Collection<Object> collect = beanDefinitions.stream().map(definition -> {
                beanCreateContext.createBean(definition);
                return definition.getBean();
            }).collect(Collectors.toList());
            paramDefinition.setParam(collect);
        }
        if (FieldOrParamTypeEnum.Simple == paramDefinition.getFieldOrParamTypeEnum()) {
            BeanDefinition injectBeanDefinition = beanDefinitionContext.getInjectBeanDefinition(paramDefinition.getParamName(), paramDefinition.getParamType());
            beanCreateContext.createBean(injectBeanDefinition);
            paramDefinition.setParam(injectBeanDefinition.getBean());
        }
        if (FieldOrParamTypeEnum.Properties == paramDefinition.getFieldOrParamTypeEnum()) {
            String paramName = paramDefinition.getParamName();
            String s = propertiesContext.getSetting().get(paramName);
            Object convert = Convert.convert(paramDefinition.getParamType(), s);
            paramDefinition.setParam(convert);
        }
    }


}
