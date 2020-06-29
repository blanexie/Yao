package xyz.xiezc.ioc.starter.common.context.impl;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.annotation.Aop;
import xyz.xiezc.ioc.starter.common.context.AnnotationContext;
import xyz.xiezc.ioc.starter.common.context.BeanCreateContext;
import xyz.xiezc.ioc.starter.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.starter.common.context.PropertiesContext;
import xyz.xiezc.ioc.starter.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.starter.common.create.impl.FactoryBeanCreateStrategy;
import xyz.xiezc.ioc.starter.common.create.impl.MethodBeanCreateStrategy;
import xyz.xiezc.ioc.starter.common.create.impl.SimpleBeanCreateStategy;
import xyz.xiezc.ioc.starter.common.definition.AnnotationAndHandler;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.common.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.common.exception.CircularDependenceException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum.Original;

/**
 * @author xiezc
 */
@Setter
@Getter
public class BeanCreateContextUtil implements BeanCreateContext {

    static Log log = LogFactory.get(BeanCreateContextUtil.class);

    protected BeanDefinitionContext beanDefinitionContext;

    protected PropertiesContext propertiesContext;
    protected ApplicationContextUtil applicationContextUtil;

    public BeanCreateContextUtil(ApplicationContextUtil applicationContextUtil) {
        this.beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();
        this.propertiesContext = applicationContextUtil.getPropertiesContext();
        this.applicationContextUtil = applicationContextUtil;
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
     * 2. 获取这个bean中的用户自定义注解，如果有的话，此时获取对应的annotationHandler来处理下
     * 3. 校验这个bean的所有依赖在不在容器中， 包括配置文件中配置值（@Value）的依赖
     * 5. 开始初始化，并且注入字段值，修改对应状态
     * 6. 判断有无切面，有切面的话，生成代理类替换生成的bean
     *
     * @param beanDefinition
     * @return
     */
    @Override
    public BeanDefinition createBean(BeanDefinition beanDefinition) {
        try {
            if (beanDefinition.getBeanStatus() == BeanStatusEnum.Completed) {
                return beanDefinition;
            }
            //2. 先初始化这个类。三种类型的有不同的初始化方法
            beanDefinition = this.buildBean(beanDefinition);
            //3. 检查有无自定义注解，如果有的话，此时获取对应的annotationHandler来处理下。Inject和Value的注解处理器很重要，需要判断依赖是否存在
            //注入依赖字段
            if (beanDefinition.getBeanStatus() == BeanStatusEnum.HalfCooked) {
                //设置字段的属性值
                dealAnnotationHandler(beanDefinition);
                beanDefinition.setBeanStatus(BeanStatusEnum.injectField);
            }
            //4 处理切面类
            //处理类上的切面
            Class<?> beanClass = beanDefinition.getBeanClass();
            Aop aop = AnnotationUtil.getAnnotation(beanClass, Aop.class);
            if (aop != null) {
                AnnotationHandler<Aop> classAnnotationHandler = applicationContextUtil.getAnnotationContext().getClassAnnotationHandler(Aop.class);
                classAnnotationHandler.processClass(aop, beanClass, beanDefinition);
            }
            //处理方法上的AOP注解
            Set<MethodDefinition> methodDefinitions = beanDefinition.getMethodDefinitions();
            for (MethodDefinition methodDefinition : methodDefinitions) {
                Aop aop1 = AnnotationUtil.getAnnotation(methodDefinition.getMethod(), Aop.class);
                if (aop1 != null) {
                    AnnotationHandler<Aop> classAnnotationHandler = applicationContextUtil.getAnnotationContext().getMethodAnnotationHandler(Aop.class);
                    classAnnotationHandler.processMethod(methodDefinition, aop1, beanDefinition);
                }
            }
            if (beanDefinition.getBeanStatus() == BeanStatusEnum.injectField) {
                beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
            }

            return beanDefinition;
        } catch (Exception e) {
            log.error("初始化出现异常，beanDefinition：{}", beanDefinition.toString(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public BeanDefinition buildBean(BeanDefinition beanDefinition) {
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
            //1. 将这个bean放入待初始化集合中，这个主要是用来解决循环依赖的判断的
            //判断循环依赖，  判断循环依赖的条件是没有这个依赖就无法创建bean。 如果bean已经HalfCooked状态了。 没必要判断
            if (isCircularDependenceBeanDefinition(beanDefinition)) {
                throw new CircularDependenceException(beanDefinition.toString());
            }
            return beanCreateStrategy.createBean(beanDefinition);
        } finally {
            //移除创建中的
            removeCreatingBeanDefinition(beanDefinition);
        }
    }


    /**
     * 自定义注解的处理
     *
     * @param beanDefinition
     */
    private void dealAnnotationHandler(BeanDefinition beanDefinition) {
        AnnotationContext annotationContext = applicationContextUtil.getAnnotationContext();
        //2.1 先获取类上的注解,并调用处理器
        Class<?> beanClass = beanDefinition.getBeanClass();
        Annotation[] annotations = AnnotationUtil.getAnnotations(beanClass, true);
        //获取所有的注解处理器并排序
        CollUtil.toList(annotations)
                .stream()
                .filter(annotation -> annotation.annotationType() != Aop.class)
                .map(annotation -> {
                    AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                    AnnotationHandler<? extends Annotation> classAnnotationHandler = annotationContext.getClassAnnotationHandler(annotation.annotationType());
                    annotationAndHandler.setAnnotationHandler(classAnnotationHandler);
                    annotationAndHandler.setAnnotation(annotation);
                    return annotationAndHandler;
                })
                .filter(annotationAndHandler -> annotationAndHandler.getAnnotationHandler() != null)
                .sorted((a, b) -> {
                    AnnotationHandler annotationHandler = a.getAnnotationHandler();
                    int ordera = annotationHandler.getOrder();

                    AnnotationHandler annotationHandlerb = b.getAnnotationHandler();
                    int orderb = annotationHandlerb.getOrder();
                    if (ordera < orderb) {
                        return -1;
                    } else {
                        return 1;
                    }
                })
                .forEach(annotationAndHandler -> {
                    AnnotationHandler annotationHandler = annotationAndHandler.getAnnotationHandler();
                    annotationHandler.processClass(annotationAndHandler.getAnnotation(), beanClass, beanDefinition);
                });

        //2.2 获取字段上的注解，并处理
        Set<FieldDefinition> fieldDefinitions = beanDefinition.getFieldDefinitions();
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            Field field = fieldDefinition.getField();
            //获取各个字段上的注解
            Annotation[] annotations1 = AnnotationUtil.getAnnotations(field, true);
            if (annotations1 == null) {
                continue;
            }
            CollUtil.toList(annotations1)
                    .stream()
                    .map(annotation -> {
                        AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                        AnnotationHandler<? extends Annotation> classAnnotationHandler = annotationContext.getFieldAnnotationHandler(annotation.annotationType());
                        annotationAndHandler.setAnnotationHandler(classAnnotationHandler);
                        annotationAndHandler.setAnnotation(annotation);
                        return annotationAndHandler;
                    }).filter(annotationAndHandler -> annotationAndHandler.getAnnotationHandler() != null)
                    .sorted((a, b) -> {
                        AnnotationHandler annotationHandler = a.getAnnotationHandler();
                        int ordera = annotationHandler.getOrder();

                        AnnotationHandler annotationHandlerb = b.getAnnotationHandler();
                        int orderb = annotationHandlerb.getOrder();
                        if (ordera < orderb) {
                            return -1;
                        } else {
                            return 1;
                        }
                    })
                    .forEach(annotationAndHandler -> {
                        AnnotationHandler fieldAnnotationHandler = annotationAndHandler.getAnnotationHandler();
                        fieldAnnotationHandler.processField(fieldDefinition, annotationAndHandler.getAnnotation(), beanDefinition);
                    });

        }
        //2.3 获取方法上的注解，并处理
        Set<MethodDefinition> methodDefinitions = beanDefinition.getMethodDefinitions();
        for (MethodDefinition methodDefinition : methodDefinitions) {
            Method method = methodDefinition.getMethod();
            //获取各个字段上的注解
            Annotation[] annotations1 = AnnotationUtil.getAnnotations(method, true);
            if (annotations1 == null) {
                continue;
            }
            CollUtil.toList(annotations1)
                    .stream()
                    .filter(annotation -> annotation.annotationType() != Aop.class)
                    .map(annotation -> {
                        AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                        AnnotationHandler<? extends Annotation> classAnnotationHandler = annotationContext.getMethodAnnotationHandler(annotation.annotationType());
                        annotationAndHandler.setAnnotationHandler(classAnnotationHandler);
                        annotationAndHandler.setAnnotation(annotation);
                        return annotationAndHandler;
                    }).filter(annotationAndHandler -> annotationAndHandler.getAnnotationHandler() != null)
                    .sorted((a, b) -> {
                        AnnotationHandler annotationHandler = a.getAnnotationHandler();
                        int ordera = annotationHandler.getOrder();

                        AnnotationHandler annotationHandlerb = b.getAnnotationHandler();
                        int orderb = annotationHandlerb.getOrder();
                        if (ordera < orderb) {
                            return -1;
                        } else {
                            return 1;
                        }
                    })
                    .forEach(annotationAndHandler -> {
                        AnnotationHandler methodAnnotationHandler = annotationAndHandler.getAnnotationHandler();
                        methodAnnotationHandler.processMethod(methodDefinition, annotationAndHandler.getAnnotation(), beanDefinition);
                    });
        }
    }

    /**
     * 移除正在创建中的bean
     *
     * @param beanDefinition
     */
    public void removeCreatingBeanDefinition(BeanDefinition beanDefinition) {
        creatingBeanDefinitions.remove(beanDefinition);
    }

    public boolean isCircularDependenceBeanDefinition(BeanDefinition beanDefinition) {
        return !creatingBeanDefinitions.add(beanDefinition);
    }
}
