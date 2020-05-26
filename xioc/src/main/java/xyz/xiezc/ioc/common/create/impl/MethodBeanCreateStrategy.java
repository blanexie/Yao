package xyz.xiezc.ioc.common.create.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Component
public class MethodBeanCreateStrategy implements BeanCreateStrategy {

    Log log = LogFactory.get(SimpleBeanCreateStategy.class);

    ApplicationContextUtil applicationContextUtil;

    @Override
    public void setApplicationContext(ApplicationContextUtil applicationContext) {
        this.applicationContextUtil = applicationContext;
    }

    @Override
    public void createBean(BeanDefinition beanDefinition) {
        BeanStatusEnum beanStatus = beanDefinition.getBeanStatus();
        if (beanStatus == BeanStatusEnum.Completed) {
            log.info("beanDefinition已经初始化了， BeanDefinition:{}", beanDefinition.toString());
            return;
        }

        //检查所有需要注入字段的依赖是否都存在
        Set<FieldDefinition> annotationFiledDefinitions = beanDefinition.getFieldDefinitions();
        applicationContextUtil.checkFieldDefinitions(annotationFiledDefinitions);
        //检查@Init注解的方法的参数是否都是空的， bean的init方法的参数必须是空的
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        applicationContextUtil.checkInitMethod(initMethodDefinition);
        //检查@Bean注解的方法的参数是否在容器中都存在
        MethodDefinition invokeMethodBean = beanDefinition.getInvokeMethodBean();
        applicationContextUtil.checkMethodParam(invokeMethodBean);

        //开始初始化
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
            //获取方法的调用方
            BeanDefinition beanDefinitionParent = invokeMethodBean.getBeanDefinition();
            //先初始化
            beanDefinitionParent = applicationContextUtil.createBean(beanDefinitionParent);
            Object bean = beanDefinitionParent.getBean();
            //获取参数
            ParamDefinition[] paramDefinitions = invokeMethodBean.getParamDefinitions();
            for (ParamDefinition paramDefinition : paramDefinitions) {
                Object param = paramDefinition.getParam();
                if (param == null) {
                    continue;
                }
                if (param instanceof BeanDefinition) {
                    BeanDefinition beanDefinitionParam = applicationContextUtil.getInjectBeanDefinition(paramDefinition.getBeanName(), paramDefinition.getParamType());
                    beanDefinitionParam = applicationContextUtil.createBean(beanDefinitionParam);
                    param = beanDefinitionParam.getBean();
                }
                paramDefinition.setParam(param);
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
            applicationContextUtil.injectFieldValue(beanDefinition);
            beanDefinition.setBeanStatus(BeanStatusEnum.injectField);
        }

        //调用init方法
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.injectField) {
            applicationContextUtil.doInitMethod(beanDefinition);
            //bean 所有对象设置完整
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        }
    }

    @Override
    public BeanTypeEnum getBeanTypeEnum() {
        return BeanTypeEnum.methodBean;
    }
}
