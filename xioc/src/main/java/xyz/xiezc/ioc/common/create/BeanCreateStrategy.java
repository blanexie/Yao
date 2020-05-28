package xyz.xiezc.ioc.common.create;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.common.NullObj;
import xyz.xiezc.ioc.common.context.BeanCreateContext;
import xyz.xiezc.ioc.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.common.context.PropertiesContext;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;
import xyz.xiezc.ioc.enums.FieldOrParamTypeEnum;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实现类需要
 */
@Data
public abstract class BeanCreateStrategy {

    protected BeanDefinitionContext beanDefinitionContext;

    protected BeanCreateContext beanCreateContext;

    protected PropertiesContext propertiesContext;

    /**
     * 校验循环依赖的set集合
     */
    static Set<BeanDefinition> creatingBeanDefinitions = new HashSet<>();

    /**
     * 创建bean
     *
     * @param beanDefinition
     */
    protected abstract BeanDefinition createBean(BeanDefinition beanDefinition);

    /**
     * 创建器类型
     *
     * @return
     */
    protected abstract BeanTypeEnum getBeanTypeEnum();

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

    protected void doInitMethod(BeanDefinition beanDefinition) {
        //调用对应bean的init方法
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        if (initMethodDefinition != null) {
            Object bean = beanDefinition.getBean();
            ReflectUtil.invoke(bean, initMethodDefinition.getMethod());
        }
    }

    /**
     * 设置bean的字段属性值
     *
     * @param beanDefinition
     */
    protected void injectFieldValue(BeanDefinition beanDefinition) {
        //设置字段的属性值
        Set<FieldDefinition> fieldDefinitions = beanDefinition.getFieldDefinitions();
        fieldDefinitions = CollectionUtil.emptyIfNull(fieldDefinitions);
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            var obj = fieldDefinition.getFieldValue();
            if (obj == null) {
                if (FieldOrParamTypeEnum.Array == fieldDefinition.getFieldOrParamTypeEnum()) {
                    List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(fieldDefinition.getFieldType());
                    List<Object> collect = beanDefinitions.stream().map(definition -> {
                        beanCreateContext.createBean(definition);
                        return definition.getBean();
                    }).collect(Collectors.toList());
                    fieldDefinition.setFieldValue(collect.toArray());
                }
                if (FieldOrParamTypeEnum.Collection == fieldDefinition.getFieldOrParamTypeEnum()) {
                    List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(fieldDefinition.getFieldType());
                    Collection<Object> collect = beanDefinitions.stream().map(definition -> {
                        beanCreateContext.createBean(definition);
                        return definition.getBean();
                    }).collect(Collectors.toList());
                    fieldDefinition.setFieldValue(collect);
                }
                if (FieldOrParamTypeEnum.Simple == fieldDefinition.getFieldOrParamTypeEnum()) {
                    BeanDefinition injectBeanDefinition = beanDefinitionContext.getInjectBeanDefinition(fieldDefinition.getFieldName(), fieldDefinition.getFieldType());
                    beanCreateContext.createBean(injectBeanDefinition);
                    fieldDefinition.setFieldValue(injectBeanDefinition.getBean());
                }
                if (FieldOrParamTypeEnum.Properties == fieldDefinition.getFieldOrParamTypeEnum()) {
                    String paramName = fieldDefinition.getFieldName();
                    String s = propertiesContext.getSetting().get(paramName);
                    Object convert = Convert.convert(fieldDefinition.getFieldType(), s);
                    fieldDefinition.setFieldValue(convert);
                }
                obj = fieldDefinition.getFieldValue();
            } else {
                if (obj instanceof BeanDefinition) {
                    BeanDefinition beanDefinitionParam = beanCreateContext.createBean((BeanDefinition) obj);
                    fieldDefinition.setFieldValue(beanDefinitionParam.getBean());
                    obj = beanDefinitionParam.getBean();
                }
            }

            //空的对象， 不用注入
            if (obj instanceof NullObj) {
                continue;
            }

            if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.factoryBean) {
                ReflectUtil.setFieldValue(beanDefinition.getFactoryBean(), fieldDefinition.getFieldName(), obj);
            } else {
                ReflectUtil.setFieldValue(beanDefinition.getBean(), fieldDefinition.getFieldName(), obj);
            }
        }
    }

    /**
     * 校验init方法
     */
    protected void checkInitMethod(MethodDefinition methodDefinition) {
        Method method = methodDefinition.getMethod();
        int parameterCount = method.getParameterCount();
        if (parameterCount > 0) {
            throw new RuntimeException("init方法不能有参数， methodDefinition:" + methodDefinition.toString());
        }
    }

    /**
     * 校验bean注解的方法的参数是否存在
     */
    protected void checkMethodParam(ParamDefinition[] paramDefinitions) {
        for (ParamDefinition paramDefinition : paramDefinitions) {
            FieldOrParamTypeEnum fieldOrParamTypeEnum = paramDefinition.getFieldOrParamTypeEnum();
            //数组类型
            if (fieldOrParamTypeEnum != FieldOrParamTypeEnum.Properties) {
                List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(paramDefinition.getParamType());
                if (beanDefinitions.isEmpty()) {
                    throw new RuntimeException("要注入的字段不存在容器中， fieldDefinition:" + paramDefinition.toString());
                }
            } else {
                String fieldName = paramDefinition.getParamName();
                Value value = AnnotationUtil.getAnnotation(paramDefinition.getParamType(), Value.class);
                if (value != null && StrUtil.isNotBlank(value.value())) {
                    fieldName = value.value();
                }
                boolean b = propertiesContext.getSetting().containsKey(fieldName);
                if (b) {
                    throw new RuntimeException("要注入的字段不存在p配置文件中， fieldDefinition:" + paramDefinition.toString());
                }
            }
        }
    }

    /**
     * 校验需要注入的字段是否存在
     *
     * @param fieldDefinitions
     */
    protected void checkFieldDefinitions(Set<FieldDefinition> fieldDefinitions) {
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            FieldOrParamTypeEnum fieldOrParamTypeEnum = fieldDefinition.getFieldOrParamTypeEnum();
            //数组类型
            if (fieldOrParamTypeEnum != FieldOrParamTypeEnum.Properties) {
                List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(fieldDefinition.getFieldType());
                if (beanDefinitions.isEmpty()) {
                    throw new RuntimeException("要注入的字段不存在容器中， fieldDefinition:" + fieldDefinition.toString());
                }
            } else {
                String fieldName = fieldDefinition.getFieldName();
                Value value = AnnotationUtil.getAnnotation(fieldDefinition.getField(), Value.class);
                if (value != null && StrUtil.isNotBlank(value.value())) {
                    fieldName = value.value();
                }
                boolean b = propertiesContext.getSetting().containsKey(fieldName);
                if (b) {
                    throw new RuntimeException("要注入的字段不存在p配置文件中， fieldDefinition:" + fieldDefinition.toString());
                }
            }
        }
    }
}
