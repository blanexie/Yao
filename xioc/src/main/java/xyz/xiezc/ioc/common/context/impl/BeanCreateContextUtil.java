package xyz.xiezc.ioc.common.context.impl;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.common.NullObj;
import xyz.xiezc.ioc.common.context.BeanCreateContext;
import xyz.xiezc.ioc.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.exception.CircularDependenceException;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
@Component
public class BeanCreateContextUtil implements BeanCreateContext {


    @Inject
    BeanDefinitionContext beanDefinitionContext;


    /**
     * 正在创建中的bean缓存，用来判断循环依赖的
     */
    Map<String, BeanDefinition> preparingBeanMap = new HashMap<>();
    /**
     *
     */
    Map<BeanTypeEnum, BeanCreateStrategy> beanCreateStrategyMap = new HashMap<>();


    @Override
    public void putBeanCreateStrategy(BeanCreateStrategy beanCreateStrategy) {
        beanCreateStrategyMap.put(beanCreateStrategy.getBeanTypeEnum(), beanCreateStrategy);
    }

    @Override
    public BeanCreateStrategy getBeanCreateStrategy(BeanTypeEnum beanTypeEnum) {
        return beanCreateStrategyMap.get(beanTypeEnum);
    }

    /**
     * 移除正在创建中的bean
     *
     * @param beanDefinition
     */
    public void removeCreatingBeanDefinition(BeanDefinition beanDefinition) {
        preparingBeanMap.remove(beanDefinition.getBeanName());
    }

    @Override
    public boolean isCircularDependenceBeanDefinition(BeanDefinition beanDefinition) {
        if (preparingBeanMap.get(beanDefinition.getBeanName()) == null) {
            preparingBeanMap.put(beanDefinition.getBeanName(), beanDefinition);
            return false;
        }
        return true;
    }

    @Override
    public BeanDefinition createBean(BeanDefinition beanDefinition) {
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Completed
                || beanDefinition.getBeanStatus() == BeanStatusEnum.injectField
        ) {
            return beanDefinition;
        }
        //判断循环依赖， 同时把BeanDefinition 放路创建中的缓存map中
        if (isCircularDependenceBeanDefinition(beanDefinition)) {
            throw new CircularDependenceException(beanDefinition.toString());
        }
        //创建 bean
        BeanTypeEnum beanTypeEnum = beanDefinition.getBeanTypeEnum();
        BeanCreateStrategy beanCreateStrategy = getBeanCreateStrategy(beanTypeEnum);
        beanCreateStrategy.createBean(beanDefinition);
        //移除缓存
        removeCreatingBeanDefinition(beanDefinition);
        return beanDefinition;
    }

    /**
     * 检查字段的依赖是否存在
     *
     * @param annotationFiledDefinitions
     */
    @Override
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
            Inject annotation = AnnotationUtil.getAnnotation(annotationFiledDefinition.getAnnotatedElement(), Inject.class);
            if (annotation != null) {
                int collectorOrArray = annotationFiledDefinition.getFieldOrParamTypeEnum();
                //判断是否是数组或者集合类型
                if (collectorOrArray == 1) {
                    List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(fieldType);
                    annotationFiledDefinition.setObj(beanDefinitions.toArray(new BeanDefinition[0]));
                } else if (collectorOrArray == 2) { //如果是集合类型
                    List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(fieldType);
                    annotationFiledDefinition.setObj(beanDefinitions);
                    return;
                }

                if (annotation.requrie()) {
                    BeanDefinition beanDefinition = beanDefinitionContext.getInjectBeanDefinition(beanName, fieldType);
                    annotationFiledDefinition.setObj(beanDefinition);
                } else {
                    BeanDefinition beanDefinition = beanDefinitionContext.getBeanDefinition(beanName, fieldType);
                    annotationFiledDefinition.setObj(beanDefinition);
                }
            }
        }
    }


    @Override
    public BeanDefinition newInstance(BeanDefinition beanDefinition) {
        //先创建类
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
            beanDefinition.setBean(bean);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }
        return beanDefinition;
    }

    /**
     * 检查方法的注入依赖是否都存在
     *
     * @param methodBeanInvoke
     */
    @Override
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
            BeanDefinition beanDefinition = beanDefinitionContext.getInjectBeanDefinition(beanName, paramType);
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
    @Override
    public void doInitMethod(BeanDefinition beanDefinition) {
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
    @Override
    public void injectFieldValue(BeanDefinition beanDefinition) {
        //设置字段的属性值
        Set<FieldDefinition> annotationFiledDefinitions = beanDefinition.getFieldDefinitions();
        annotationFiledDefinitions = CollectionUtil.emptyIfNull(annotationFiledDefinitions);
        for (FieldDefinition fieldDefinition : annotationFiledDefinitions) {
            var obj = fieldDefinition.getObj();
            if (obj instanceof BeanDefinition) {
                BeanDefinition fieldBeanDefinition = (BeanDefinition) obj;
                fieldBeanDefinition = this.createBean(fieldBeanDefinition);
                obj = fieldBeanDefinition.getBean();
            }
            //空的对象， 不用注入
            if (obj instanceof NullObj) {
                continue;
            }
            //数组
            if (fieldDefinition.getFieldOrParamTypeEnum() == 1) {
                List<BeanDefinition> obj1 = (List<BeanDefinition>) fieldDefinition.getObj();
                Object[] objects = obj1.stream().map(beanDefinition1 -> beanDefinition.getBean()).collect(Collectors.toList()).toArray();
                if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.factoryBean) {
                    ReflectUtil.setFieldValue(beanDefinition.getFactoryBean(), fieldDefinition.getFieldName(), objects);
                } else {
                    ReflectUtil.setFieldValue(beanDefinition.getBean(), fieldDefinition.getFieldName(), objects);
                }
                return;
            }
            //集合
            if (fieldDefinition.getFieldOrParamTypeEnum() == 2) {
                List<BeanDefinition> obj1 = (List<BeanDefinition>) fieldDefinition.getObj();
                List<Object> collect = obj1.stream()
                        .map(fieldBeanDefinition -> {
                            Object bean = fieldBeanDefinition.getBean();
                            return bean;
                        }).collect(Collectors.toList());
                if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.factoryBean) {
                    ReflectUtil.setFieldValue(beanDefinition.getFactoryBean(), fieldDefinition.getFieldName(), collect);
                } else {
                    ReflectUtil.setFieldValue(beanDefinition.getBean(), fieldDefinition.getFieldName(), collect);
                }
                return;
            }

            if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.factoryBean) {
                ReflectUtil.setFieldValue(beanDefinition.getFactoryBean(), fieldDefinition.getFieldName(), obj);
            } else {
                ReflectUtil.setFieldValue(beanDefinition.getBean(), fieldDefinition.getFieldName(), obj);
            }
        }
    }

}
