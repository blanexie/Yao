package xyz.xiezc.ioc.system.common.context.impl;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.system.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.system.common.context.AnnotationContext;
import xyz.xiezc.ioc.system.common.context.BeanCreateContext;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.context.PropertiesContext;
import xyz.xiezc.ioc.system.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.system.common.create.impl.FactoryBeanCreateStrategy;
import xyz.xiezc.ioc.system.common.create.impl.MethodBeanCreateStrategy;
import xyz.xiezc.ioc.system.common.create.impl.SimpleBeanCreateStategy;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.system.common.exception.CircularDependenceException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
            //1. 将这个bean放入待初始化集合中，这个主要是用来解决循环依赖的判断的
            //判断循环依赖， 同时把BeanDefinition 放路创建中的缓存map中
            if (isCircularDependenceBeanDefinition(beanDefinition)) {
                throw new CircularDependenceException(beanDefinition.toString());
            }
            if (beanDefinition.getBeanStatus() == BeanStatusEnum.Completed) {
                log.info("BeanDefinition:{}", beanDefinition.toString());
                return beanDefinition;
            }
            //2. 先初始化这个类。三种类型的有不同的初始化方法
            beanDefinition = this.buildBean(beanDefinition);
            //3. 检查有无自定义注解，如果有的话，此时获取对应的annotationHandler来处理下。Inject和Value的注解处理器很重要，需要判断依赖是否存在
            //注入字段
            if (beanDefinition.getBeanStatus() == BeanStatusEnum.HalfCooked) {
                //设置字段的属性值
                dealAnnotationHandler(beanDefinition);
                beanDefinition.setBeanStatus(BeanStatusEnum.injectField);
            }
            //todo  4. 判断有无切面，有切面的话，生成代理类替换生成的bean


            return beanDefinition;
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            //移除创建中的
            removeCreatingBeanDefinition(beanDefinition);
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
        return beanCreateStrategy.createBean(beanDefinition);
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
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> aClass = annotation.annotationType();
            AnnotationHandler<? extends Annotation> classAnnotationHandler = annotationContext.getClassAnnotationHandler(aClass);
            if (classAnnotationHandler != null) {
                this.buildBean(beanDefinition);
                classAnnotationHandler.processClass(annotation, beanClass, beanDefinition);
            }
        }
        //2.2 获取字段上的注解，并处理
        Set<FieldDefinition> fieldDefinitions = beanDefinition.getFieldDefinitions();
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            Field field = fieldDefinition.getField();
            //获取各个字段上的注解
            Annotation[] annotations1 = AnnotationUtil.getAnnotations(field, true);
            if (annotations1 == null) {
                continue;
            }
            for (Annotation annotation : annotations1) {
                //每个注解都检查下有无处理器来处理
                Class<? extends Annotation> aClass = annotation.annotationType();
                AnnotationHandler<? extends Annotation> fieldAnnotationHandler = annotationContext.getFieldAnnotationHandler(aClass);
                if (fieldAnnotationHandler != null) {
                    this.buildBean(beanDefinition);
                    fieldAnnotationHandler.processField(fieldDefinition, annotation, beanDefinition);
                }
            }
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
            for (Annotation annotation : annotations1) {
                //每个注解都检查下有无处理器来处理
                Class<? extends Annotation> aClass = annotation.annotationType();
                AnnotationHandler<? extends Annotation> methodAnnotationHandler = annotationContext.getMethodAnnotationHandler(aClass);
                if (methodAnnotationHandler != null) {
                    this.buildBean(beanDefinition);
                    methodAnnotationHandler.processMethod(methodDefinition, annotation, beanDefinition);
                }
            }
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
