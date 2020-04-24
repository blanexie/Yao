package xyz.xiezc.ioc.common.create;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class MethodBeanCreateStrategy implements BeanFactoryStrategy {

    Log log = LogFactory.get(BeanCreateStrategy.class);

    BeanFactoryUtil beanFactoryUtil;

    @Override
    public void createBean(BeanDefinition beanDefinition) {
        BeanStatusEnum beanStatus = beanDefinition.getBeanStatus();
        if (beanStatus == BeanStatusEnum.Completed) {
            log.info("beanDefinition已经初始化了， BeanDefinition:{}", beanDefinition.toString());
            return;
        }

        ContextUtil contextUtil = beanFactoryUtil.getContextUtil();

        //检查所有需要注入字段的依赖是否都存在
        Set<FieldDefinition> annotationFiledDefinitions = beanDefinition.getAnnotationFiledDefinitions();
        beanFactoryUtil.checkFieldDefinitions(annotationFiledDefinitions);
        //检查@Init注解的方法的参数是否都是空的， bean的init方法的参数必须是空的
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        beanFactoryUtil.checkInitMethod(initMethodDefinition);
        //检查@Bean注解的方法的参数是否在容器中都存在
        MethodDefinition invokeMethodBean = beanDefinition.getInvokeMethodBean();
        beanFactoryUtil.checkMethodParam(invokeMethodBean);

        //开始初始化
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
            //获取方法的调用方
            BeanDefinition beanDefinitionParent = invokeMethodBean.getBeanDefinition();
            //先初始化
            beanDefinitionParent = beanFactoryUtil.createBean(beanDefinitionParent);
            Object bean = beanDefinitionParent.getBean();
            //获取参数
            ParamDefinition[] paramDefinitions = invokeMethodBean.getParamDefinitions();
            for (ParamDefinition paramDefinition : paramDefinitions) {
                Object param = paramDefinition.getParam();
                if (param == null) {
                    continue;
                }
                if (param instanceof BeanDefinition) {
                    BeanDefinition beanDefinitionParam = contextUtil.getBeanDefinition(paramDefinition.getBeanName(), paramDefinition.getParamType());
                    beanDefinitionParam = beanFactoryUtil.createBean(beanDefinitionParam);
                    param = beanDefinitionParam.getBean();
                }
                paramDefinition.setParam(param);
            }
            Object[] objects = CollUtil.newArrayList(paramDefinitions).stream()
                    .map(paramDefinition ->
                            paramDefinition.getParam()
                    ).collect(Collectors.toList()).toArray();
            //调用方法， 获取结果
            Object invoke = ReflectUtil.invoke(bean, invokeMethodBean.getMethodName(), objects);
            beanDefinition.setBean(invoke);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }

        //注入字段
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.HalfCooked) {
            //设置字段的属性值
            beanFactoryUtil.injectFieldValue(beanDefinition);
            beanDefinition.setBeanStatus(BeanStatusEnum.injectField);
        }

        //调用init方法
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.injectField) {
            beanFactoryUtil.doInitMethod(beanDefinition);
            //bean 所有对象设置完整
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        }
    }

    @Override
    public BeanTypeEnum getBeanTypeEnum() {
        return BeanTypeEnum.methodBean;
    }
}
