package xyz.xiezc.ioc.starter;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.annotation.EventListener;
import xyz.xiezc.ioc.starter.annotation.Configuration;
import xyz.xiezc.ioc.starter.annotation.SystemLoad;
import xyz.xiezc.ioc.starter.annotation.handler.*;
import xyz.xiezc.ioc.starter.common.context.AnnotationContext;
import xyz.xiezc.ioc.starter.common.context.BeanCreateContext;
import xyz.xiezc.ioc.starter.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.starter.common.context.EventPublisherContext;
import xyz.xiezc.ioc.starter.common.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.event.ApplicationEvent;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.event.ApplicationListener;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static xyz.xiezc.ioc.starter.common.enums.EventNameConstant.*;

/**
 * 超级简单的依赖注入小框架
 * <p>
 * 1. 不支持注解的注解
 * 2. 目前只支持单例的bean
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:17
 */
@Data
public final class Xioc {

    private static Log log = LogFactory.get(Xioc.class);

    /**
     * 装载依赖的容器. 当依赖全部注入完成的时候,这个集合会清空
     */
    private ApplicationContextUtil applicationContextUtil;

    public static Set<Class<? extends ApplicationListener>> systemListener = new HashSet<>();


    private static Xioc xioc;
    public static Class bootClass;

    /**
     * 启动方法,
     *
     * @param clazz 传入的启动类, 以这个启动类所在目录为根目录开始扫描bean类
     */
    public static ApplicationContextUtil run(Class<?> clazz) {
        xioc = new Xioc();
        bootClass = clazz;
        //校验启动类上是否有@Configuration注解
        if (AnnotationUtil.getAnnotation(clazz, Configuration.class) == null) {
            throw new RuntimeException("请在启动类" + clazz.getName() + "上增加@Configuration注解");
        }
        //new创建ApplicationContextUtil类， beanDefinitionContext，annotationContext，propertiesContext，eventPublisherContext 等实现类
        ApplicationContextUtil applicationContextUtil = new ApplicationContextUtil();
        EventPublisherContext eventPublisherContext = applicationContextUtil.getEventPublisherContext();
        AnnotationContext annotationContext = applicationContextUtil.getAnnotationContext();
        BeanCreateContext beanCreateContext = applicationContextUtil.getBeanCreateContext();
        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();

        xioc.applicationContextUtil = applicationContextUtil;
        eventPublisherContext.publisherEvent(new ApplicationEvent(EventNameConstant.LoadApplicationContextUtil));
        log.info("ApplicationContextUtil加载完成.............");

        //## 调用propertiesContext的loadProperties方法，先加载配置文件到容器中。
        applicationContextUtil.getPropertiesContext().loadProperties();
        eventPublisherContext.publisherEvent(new ApplicationEvent(EventNameConstant.LoadPropertiesContext));
        log.info("PropertiesContext加载完成..................");

        //## 先初始化系统中的annotationHandler类，并将注解处理器全部放入annotationContext中.
        loadSystemBeanDefinition(applicationContextUtil);
        eventPublisherContext.publisherEvent(new ApplicationEvent(LoadSystemBeanDefinition));
        log.info("加载系统BeanDefinition类完成........................");

        //## 扫描路径下的所有的需要需要注入容器中的类， 主要是Component和Configuration两个注解注释的类
        applicationContextUtil.loadBeanDefinitions(clazz);
        eventPublisherContext.publisherEvent(new ApplicationEvent(loadBeanDefinitions));
        log.info("加载BeanDefinition完成........................");

        //## 遍历所有的beanDefinition，优先初始化用户自定义的annotationHandler类和ApplicationListener类。
        loadAnnotationHandlerAndApplicationListener(annotationContext, beanCreateContext, beanDefinitionContext, eventPublisherContext);
        //## 遍历剩下的的beanDefinition，并且初始化, 优先初始化切面类
        Collection<BeanDefinition> allBeanDefintion = beanDefinitionContext.getAllBeanDefintion();
        CopyOnWriteArrayList<BeanDefinition> copyOnWriteArrayList = new CopyOnWriteArrayList<>(allBeanDefintion);
        for (BeanDefinition beanDefinition : copyOnWriteArrayList) {
            beanCreateContext.createBean(beanDefinition);
        }
        log.info("初始化所有类完成..........................");
        eventPublisherContext.publisherEvent(new ApplicationEvent(EventNameConstant.XiocEnd));

        return xioc.getApplicationContextUtil();
    }

    /**
     * 初始化加载AnnotationHandler和ApplicationListener
     *
     * @param annotationContext
     * @param beanCreateContext
     * @param beanDefinitionContext
     * @param eventPublisherContext
     */
    private static void loadAnnotationHandlerAndApplicationListener(AnnotationContext annotationContext, BeanCreateContext beanCreateContext, BeanDefinitionContext beanDefinitionContext, EventPublisherContext eventPublisherContext) {
        //1. 先初始化注解处理器
        List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(AnnotationHandler.class);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            AnnotationHandler<Annotation> annotationAnnotationHandler = annotationContext.getAnnotationAnnotationHandler(beanClass);
            if (annotationAnnotationHandler == null) {
                //初始化注解处理器
                BeanDefinition bean = beanCreateContext.createBean(beanDefinition);
                annotationContext.addAnnotationHandler(bean.getBean());
            }
        }
        log.info("加载所有的注解处理器类.....................");
        eventPublisherContext.publisherEvent(new ApplicationEvent(LoadAnnotationHandler));

        //2. 初始化相关的ApplicationListener， 需要保证在前面的事件先初始化
        List<BeanDefinition> beanDefinitionList = beanDefinitionContext.getBeanDefinitions(ApplicationListener.class);
        for (BeanDefinition beanDefinition : beanDefinitionList) {
            BeanDefinition bean = beanCreateContext.createBean(beanDefinition);
            ApplicationListener applicationListener = bean.getBean();

            Class<?> beanClass = beanDefinition.getBeanClass();
            EventListener eventListener = AnnotationUtil.getAnnotation(beanClass, EventListener.class);
            String[] eventNames = eventListener.eventName();
            for (String eventName : eventNames) {
                eventPublisherContext.addApplicationListener(eventName, applicationListener);
            }
        }
        log.info("加载所有的事件处理器类.....................");
        eventPublisherContext.publisherEvent(new ApplicationEvent(LoadApplicationListener));
    }

    /**
     * 初始化系统中的时间监听器
     *
     * @param beanDefinitionContext
     * @param eventPublisherContext
     */
    private static void loadSystemListener(BeanDefinitionContext beanDefinitionContext, EventPublisherContext eventPublisherContext) {
        for (Class<? extends ApplicationListener> aClass : systemListener) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBean(ReflectUtil.newInstanceIfPossible(aClass));
            beanDefinition.setBeanClass(aClass);
            beanDefinition.setBeanName(aClass.getSimpleName());
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
            beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
            beanDefinitionContext.addBeanDefinition(beanDefinition.getBeanName(), beanDefinition.getBeanClass(), beanDefinition);
            EventListener annotation = AnnotationUtil.getAnnotation(aClass, EventListener.class);
            if (annotation == null) {
                throw new RuntimeException(aClass.getSimpleName() + "系统监听器请配置EventListener注解");
            }
            String[] strings = annotation.eventName();
            if (strings == null) {
                throw new RuntimeException(aClass.getSimpleName() + "系统监听器请配置EventListener注解");
            }
            for (String string : strings) {
                eventPublisherContext.addApplicationListener(string, beanDefinition.getBean());
            }
        }
    }

    /**
     * 先加载系统中的beanDefintion, 这里加载的beanDefintion都是Completed状态的，并且没有依赖注入的情况
     *
     * @param applicationContextUtil
     */
    private static void loadSystemBeanDefinition(ApplicationContextUtil applicationContextUtil) {
        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();
        AnnotationContext annotationContext = applicationContextUtil.getAnnotationContext();
        EventPublisherContext eventPublisherContext = applicationContextUtil.getEventPublisherContext();
        Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation(applicationContextUtil.starterPackage, SystemLoad.class);
        Set<Class<?>> classes1 = ClassUtil.scanPackageByAnnotation(ClassUtil.getPackage(bootClass), SystemLoad.class);
        classes1.forEach(clazz -> {
            classes.add(clazz);
        });


        for (Class<?> aClass : classes) {
            //是注解处理器
            if (ClassUtil.isAssignable(AnnotationHandler.class, aClass)) {
                AnnotationHandler annotationHandler = (AnnotationHandler) ReflectUtil.newInstanceIfPossible(aClass);
                annotationContext.addAnnotationHandler(annotationHandler);
                newBeanDefinition(beanDefinitionContext, aClass, annotationHandler);
            } else
                //是事件监听器
                if (ClassUtil.isAssignable(ApplicationListener.class, aClass)) {
                    EventListener eventListener = AnnotationUtil.getAnnotation(aClass, EventListener.class);
                    if (eventListener == null) {
                        throw new RuntimeException(aClass.getSimpleName() + "系统监听器请配置EventListener注解");
                    }
                    String[] eventNames = eventListener.eventName();
                    if (eventNames == null) {
                        throw new RuntimeException(aClass.getSimpleName() + "系统监听器请配置EventListener注解");
                    }
                    ApplicationListener applicationListener = (ApplicationListener) ReflectUtil.newInstanceIfPossible(aClass);
                    newBeanDefinition(beanDefinitionContext, aClass, applicationListener);

                    for (String eventName : eventNames) {
                        eventPublisherContext.addApplicationListener(eventName, applicationListener);
                    }
                } else {
                    //普通的bean
                    Object bean = ReflectUtil.newInstanceIfPossible(aClass);
                    newBeanDefinition(beanDefinitionContext, aClass, bean);
                }
        }


    }

    private static BeanDefinition newBeanDefinition(BeanDefinitionContext beanDefinitionContext, Class<?> aClass, Object annotationHandler) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        beanDefinition.setBeanClass(aClass);
        beanDefinition.setBeanName(aClass.getName());
        beanDefinition.setBean(annotationHandler);
        beanDefinitionContext.addBeanDefinition(beanDefinition.getBeanName(), beanDefinition.getBeanClass(), beanDefinition);
        return beanDefinition;
    }

    public static ApplicationContextUtil getApplicationContext() {
        return xioc.getApplicationContextUtil();
    }


    /**
     *
     */
    private static <T> BeanDefinition newInstance(Class<T> clazz, T t) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        beanDefinition.setBeanClass(clazz);
        beanDefinition.setBeanName(clazz.getName());
        beanDefinition.setBean(t);
        return beanDefinition;
    }

}
