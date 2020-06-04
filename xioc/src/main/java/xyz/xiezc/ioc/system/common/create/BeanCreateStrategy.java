package xyz.xiezc.ioc.system.common.create;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import xyz.xiezc.ioc.system.annotation.Value;
import xyz.xiezc.ioc.system.common.NullObj;
import xyz.xiezc.ioc.system.common.context.BeanCreateContext;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.context.PropertiesContext;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.definition.ParamDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.system.common.enums.FieldOrParamTypeEnum;

import java.lang.reflect.Method;
import java.util.Collection;
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
     * @param beanDefinition
     */
    public abstract BeanDefinition createBean(BeanDefinition beanDefinition);

    /**
     * 创建器类型
     *
     * @return
     */
    protected abstract BeanTypeEnum getBeanTypeEnum();

    /**
     * 校验init方法
     */
    protected void checkInitMethod(MethodDefinition methodDefinition) {
        if (methodDefinition == null) {
            return;
        }
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
            //已经有值了，就不用校验
            if (paramDefinition.getParam() != null) {
                continue;
            }
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
            if(fieldDefinition.getFieldValue()!=null){
               continue;
            }
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
                    throw new RuntimeException("要注入的字段不存在p配置文件中，fieldDefinition:" + fieldDefinition.toString());
                }
            }
        }
    }
}
