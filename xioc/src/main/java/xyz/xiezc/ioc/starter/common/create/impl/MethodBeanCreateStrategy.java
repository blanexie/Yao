package xyz.xiezc.ioc.starter.common.create.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.starter.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.common.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.common.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.common.enums.FieldOrParamTypeEnum;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class MethodBeanCreateStrategy extends BeanCreateStrategy {

    Log log = LogFactory.get(SimpleBeanCreateStategy.class);


    @Override
    public BeanTypeEnum getBeanTypeEnum() {
        return BeanTypeEnum.methodBean;
    }


    @Override
    public BeanDefinition createBean(BeanDefinition beanDefinition) {
        BeanStatusEnum beanStatus = beanDefinition.getBeanStatus();
        if (beanStatus != BeanStatusEnum.Original) {
            return beanDefinition;
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
            //调用方法，获取结果
            Object invoke = ReflectUtil.invoke(bean, invokeMethodBean.getMethod(), objects);
            beanDefinition.setBean(invoke);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }

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
