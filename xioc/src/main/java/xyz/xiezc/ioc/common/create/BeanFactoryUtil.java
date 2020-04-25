package xyz.xiezc.ioc.common.create;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BeanFactoryUtil {

    @Setter
    @Getter
    ContextUtil contextUtil;

    Map<BeanTypeEnum, BeanFactoryStrategy> beanCreateStrategyMap = new HashMap<>();

    /**
     * 初始化在中的bean缓存，用来判断循环依赖的
     */
    @Setter
    @Getter
    Map<String, BeanDefinition> preparingBeanMap = new HashMap<>();

    public void addBeanFactoryStrategy(BeanFactoryStrategy strategy) {
        strategy.setBeanFactoryUtil(this);
        beanCreateStrategyMap.put(strategy.getBeanTypeEnum(), strategy);
    }

    public BeanDefinition createBean(BeanDefinition beanDefinition) {
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Completed
                || beanDefinition.getBeanStatus() == BeanStatusEnum.injectField
        ) {
            return beanDefinition;
        }
        String beanName = beanDefinition.getBeanName();

        if (preparingBeanMap.get(beanName) == null) {
            throw new RuntimeException("出现循环依赖，依赖的bean：" + beanDefinition.toString());
        }
        //beanName 放入缓存中
        preparingBeanMap.put(beanName, beanDefinition);
        //创建 bean
        BeanTypeEnum beanTypeEnum = beanDefinition.getBeanTypeEnum();
        BeanFactoryStrategy beanFactoryStrategy = beanCreateStrategyMap.get(beanTypeEnum);
        beanFactoryStrategy.createBean(beanDefinition);
        //移除缓存
        preparingBeanMap.remove(beanName);
        return beanDefinition;
    }

    /**
     * 检查字段的依赖是否存在
     *
     * @param annotationFiledDefinitions
     */
    public void checkFieldDefinitions(Set<FieldDefinition> annotationFiledDefinitions) {
        annotationFiledDefinitions = CollectionUtil.emptyIfNull(annotationFiledDefinitions);
        for (FieldDefinition annotationFiledDefinition : annotationFiledDefinitions) {
            if (annotationFiledDefinition.getObj() != null) {
                continue;
            }
            /**
             *  annotationFiledDefinitions 中保存的是bean中的所有包含注解的字段，
             *  但这些注解不一定是Inject和Value注解，可能还有一些自定义注解。
             *  所以如果beanName字段有值，就说明需要中容器中取得对应的bean来放入FieldDefinition的bean字段中。
             *  Value注解是从配置中取值放入FieldDefinition的bean字段中。
             *  后面初始化的时候会使用FieldDefinition的bean字段
             **/
            if (StrUtil.isBlank(annotationFiledDefinition.getBeanName())) {
                continue;
            }

            String beanName = annotationFiledDefinition.getBeanName();
            Class<?> fieldType = annotationFiledDefinition.getFieldType();
            BeanDefinition beanDefinition = contextUtil.getInjectBeanDefinition(beanName, fieldType);
            annotationFiledDefinition.setObj(beanDefinition);
        }
    }

    /**
     * 检查方法的注入依赖是否都存在
     *
     * @param methodBeanInvoke
     */
    public void checkMethodParam(MethodDefinition methodBeanInvoke) {
        if (methodBeanInvoke == null) {
            return;
        }
        ParamDefinition[] paramDefinitions = methodBeanInvoke.getParamDefinitions();
        if (paramDefinitions == null) {
            return;
        }
        for (ParamDefinition paramDefinition : paramDefinitions) {
            Object param = paramDefinition.getParam();
            if (param != null) {
                continue;
            }
            String beanName = paramDefinition.getBeanName();
            Class paramType = paramDefinition.getParamType();
            BeanDefinition beanDefinition = contextUtil.getInjectBeanDefinition(beanName, paramType);
            paramDefinition.setParam(beanDefinition);
        }
    }

    /**
     * bean的init方法必须都是无参数的
     *
     * @param methodBeanInvoke
     */
    public void checkInitMethod(MethodDefinition methodBeanInvoke) {
        if (methodBeanInvoke == null) {
            return;
        }
        ParamDefinition[] paramDefinitions = methodBeanInvoke.getParamDefinitions();
        if (paramDefinitions == null || paramDefinitions.length == 0) {
            return;
        }
        throw new RuntimeException("bean的init方法必须都是无参数的");
    }

    /**
     * 调用bean
     *
     * @param beanDefinition
     */
    public void doInitMethod(BeanDefinition beanDefinition) {
        //调用对应bean的init方法
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        if (initMethodDefinition != null) {
            Object bean = beanDefinition.getBean();
            ReflectUtil.invoke(bean, initMethodDefinition.getMethodName());
        }
    }

    /**
     * 设置bean的字段属性值
     *
     * @param beanDefinition
     */
    public void injectFieldValue(BeanDefinition beanDefinition) {
        //设置字段的属性值
        Set<FieldDefinition> annotationFiledDefinitions = beanDefinition.getAnnotationFiledDefinitions();
        annotationFiledDefinitions = CollectionUtil.emptyIfNull(annotationFiledDefinitions);
        for (FieldDefinition fieldDefinition : annotationFiledDefinitions) {
            Object obj = fieldDefinition.getObj();
            if (obj instanceof BeanDefinition) {
                obj = this.createBean((BeanDefinition) obj);
            }
            ReflectUtil.setFieldValue(beanDefinition.getBean(), fieldDefinition.getFieldName(), obj);
        }
    }


}
