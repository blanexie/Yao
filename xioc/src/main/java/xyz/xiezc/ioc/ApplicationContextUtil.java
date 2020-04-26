package xyz.xiezc.ioc;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import lombok.Data;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.common.context.*;
import xyz.xiezc.ioc.common.context.impl.*;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 容器，装载bean的容器,以及其他的所有的需要存放的对象都在这里
 */
@Data
public class ApplicationContextUtil implements BeanDefinitionContext, AnnotationContext, EventPublisherContext, BeanCreateContext, PropertiesContext {

    Log log = LogFactory.get(ApplicationContextUtil.class);

    /**
     * 全局线程池
     */
    public static ExecutorService executorService = ThreadUtil.newExecutor(1, 100);

    private BeanDefinitionContext beanDefinitionContext = new BeanDefinitionContextUtil();

    private AnnotationContext annotationContext = new AnnotationContextUtil();

    private BeanCreateContext beanCreateContext = new BeanCreateContextUtil(beanDefinitionContext);

    private PropertiesContext propertiesContext = new PropertiesContextUtil();

    private EventPublisherContext eventPublisherContext = new EventPublisherContextUtil();

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
        beanCreateContext.putBeanCreateStrategy(beanCreateStrategy);
    }

    @Override
    public BeanCreateStrategy getBeanCreateStrategy(BeanTypeEnum beanTypeEnum) {
        return beanCreateContext.getBeanCreateStrategy(beanTypeEnum);
    }

    @Override
    public void removeCreatingBeanDefinition(BeanDefinition beanDefinition) {
        beanCreateContext.removeCreatingBeanDefinition(beanDefinition);
    }

    @Override
    public boolean isCircularDependenceBeanDefinition(BeanDefinition beanDefinition) {
        return beanCreateContext.isCircularDependenceBeanDefinition(beanDefinition);
    }

    @Override
    public BeanDefinition createBean(BeanDefinition beanDefinition) {
        return beanCreateContext.createBean(beanDefinition);
    }

    @Override
    public void checkFieldDefinitions(Set<FieldDefinition> annotationFiledDefinitions) {
        beanCreateContext.checkFieldDefinitions(annotationFiledDefinitions);
    }

    @Override
    public BeanDefinition newInstance(BeanDefinition beanDefinition) {
        return beanCreateContext.newInstance(beanDefinition);
    }

    @Override
    public void checkMethodParam(MethodDefinition methodBeanInvoke) {
        beanCreateContext.checkMethodParam(methodBeanInvoke);
    }

    @Override
    public void checkInitMethod(MethodDefinition methodBeanInvoke) {
        beanCreateContext.checkInitMethod(methodBeanInvoke);
    }

    @Override
    public void doInitMethod(BeanDefinition beanDefinition) {
        beanCreateContext.doInitMethod(beanDefinition);
    }

    @Override
    public void injectFieldValue(BeanDefinition beanDefinition) {
        beanCreateContext.injectFieldValue(beanDefinition);
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

    @Override
    public void addApplicationListener(String eventName, ApplicationListener listener) {
        eventPublisherContext.addApplicationListener(eventName, listener);
    }

    @Override
    public void removeApplicationListener(String eventName) {
        eventPublisherContext.removeApplicationListener(eventName);
    }

    @Override
    public void removeAllListeners() {
        eventPublisherContext.removeAllListeners();
    }

    @Override
    public void publisherEvent(ApplicationEvent event) {
        eventPublisherContext.publisherEvent(event);
    }
}
