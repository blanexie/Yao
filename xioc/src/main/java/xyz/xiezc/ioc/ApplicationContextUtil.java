package xyz.xiezc.ioc;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import lombok.Data;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.common.context.AnnotationContext;
import xyz.xiezc.ioc.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.common.context.PreparingBeanDefinitionContext;
import xyz.xiezc.ioc.common.context.PropertiesContext;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.create.impl.SimpleBeanCreateStategy;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * 容器，装载bean的容器,以及其他的所有的需要存放的对象都在这里
 */
@Data
public class ApplicationContextUtil implements BeanDefinitionContext, AnnotationContext, PreparingBeanDefinitionContext, PropertiesContext {

    Log log = LogFactory.get(ApplicationContextUtil.class);

    private AnnotationContext annotationContext;

    private PreparingBeanDefinitionContext preparingBeanDefinitionContext;

    private PropertiesContext propertiesContext;

    private BeanDefinitionContext beanDefinitionContext;

    @Override
    public <T extends Annotation> void addAnnotationHandler(AnnotationHandler<T> annotationHandler) {
        annotationContext.addAnnotationHandler(annotationHandler);
    }

    @Override
    public void addNotHandleAnnotation(Class<? extends Annotation> clazz) {
        annotationContext.addNotHandleAnnotation(clazz);
    }

    @Override
    public boolean isNotHandleAnnotation(Class<? extends Annotation> clazz) {
        return annotationContext.isNotHandleAnnotation(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getClassAnnotationHandler(Class<T> clazz) {
        return annotationContext.getClassAnnotationHandler(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getMethodAnnotationHandler(Class<T> clazz) {
        return annotationContext.getMethodAnnotationHandler(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getFieldAnnotationHandler(Class<T> clazz) {
        return annotationContext.getFieldAnnotationHandler(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getAnnotationAnnotationHandler(Class<T> clazz) {
        return annotationContext.getAnnotationAnnotationHandler(clazz);
    }

    @Override
    public <T extends Annotation> AnnotationHandler<T> getParameterAnnotationHandler(Class<T> clazz) {
        return annotationContext.getParameterAnnotationHandler(clazz);
    }


    @Override
    public void putBeanCreateStrategy(BeanCreateStrategy beanCreateStrategy) {
        preparingBeanDefinitionContext.putBeanCreateStrategy(beanCreateStrategy);
    }

    @Override
    public BeanCreateStrategy getBeanCreateStrategy(BeanTypeEnum beanTypeEnum) {
        return preparingBeanDefinitionContext.getBeanCreateStrategy(beanTypeEnum);
    }

    @Override
    public void removeCreatingBeanDefinition(BeanDefinition beanDefinition) {
        preparingBeanDefinitionContext.removeCreatingBeanDefinition(beanDefinition);
    }

    @Override
    public boolean isCircularDependenceBeanDefinition(BeanDefinition beanDefinition) {
        return preparingBeanDefinitionContext.isCircularDependenceBeanDefinition(beanDefinition);
    }

    @Override
    public void addSetting(Setting setting) {
        propertiesContext.addSetting(setting);
    }

    @Override
    public Setting getSetting() {
        return propertiesContext.getSetting();
    }

    @Override
    public Collection<BeanDefinition> getAllBeanDefintion() {
        return beanDefinitionContext.getAllBeanDefintion();
    }

    @Override
    public void addBeanDefinition(String beanName, Class<?> beanClass, BeanDefinition beanDefinition) {
        beanDefinitionContext.addBeanDefinition(beanName, beanClass, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName, Class<?> beanClass) {
        return beanDefinitionContext.getBeanDefinition(beanName, beanClass);
    }

    @Override
    public BeanDefinition getInjectBeanDefinition(String beanName, Class<?> beanClass) {
        return beanDefinitionContext.getInjectBeanDefinition(beanName, beanClass);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionContext.getBeanDefinition(beanName);
    }

    @Override
    public List<BeanDefinition> getBeanDefinitions(String beanName) {
        return beanDefinitionContext.getBeanDefinitions(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(Class<?> beanClass) {
        return beanDefinitionContext.getBeanDefinition(beanClass);
    }

    @Override
    public List<BeanDefinition> getBeanDefinitions(Class<?> beanClass) {
        return beanDefinitionContext.getBeanDefinitions(beanClass);
    }
}
