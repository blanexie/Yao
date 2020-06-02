package xyz.xiezc.ioc.system;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.system.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.EventListener;
import xyz.xiezc.ioc.system.annotation.Configuration;
import xyz.xiezc.ioc.system.annotation.handler.*;
import xyz.xiezc.ioc.system.common.context.AnnotationContext;
import xyz.xiezc.ioc.system.common.context.BeanCreateContext;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.context.EventPublisherContext;
import xyz.xiezc.ioc.system.event.ApplicationEvent;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.system.event.ApplicationListener;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    /**
     * 加载其他starter需要扫描的package路径
     */
    public final String starterPackage = "xyz.xiezc.ioc.starter";

    private static Xioc xioc;

    /**
     * 启动方法,
     *
     * @param clazz 传入的启动类, 以这个启动类所在目录为根目录开始扫描bean类
     */
    public static ApplicationContextUtil run(Class<?> clazz) {
        xioc = new Xioc();
        //校验启动类上是否有@Configuration注解
        if (AnnotationUtil.getAnnotation(clazz, Configuration.class) == null) {
            throw new RuntimeException("请在启动类" + clazz.getName() + "上增加@Configuration注解");
        }
        //new创建ApplicationContextUtil类， beanDefinitionContext，annotationContext，propertiesContext，eventPublisherContext 等实现类
        ApplicationContextUtil applicationContextUtil = new ApplicationContextUtil();
        xioc.applicationContextUtil = applicationContextUtil;
        log.info("ApplicationContextUtil加载完成.............");
        //## 调用propertiesContext的loadProperties方法，先加载配置文件到容器中。
        applicationContextUtil.getPropertiesContext().loadProperties();
        log.info("PropertiesContext加载完成..................");

        //## 先初始化系统中的annotationHandler类，并将注解处理器全部放入annotationContext中.
        loadAnnotationHandler(applicationContextUtil);
        log.info("加载注解初始化类完成........................");

        //## 扫描路径下的所有的需要需要注入容器中的类， 主要是Component和Configuration两个注解注释的类
        applicationContextUtil.loadBeanDefinitions(clazz);
        log.info("加载注解初始化类完成........................");

        //## 遍历所有的beanDefinition，优先初始化用户自定义的annotationHandler类和ApplicationListener类。
        AnnotationContext annotationContext = applicationContextUtil.getAnnotationContext();
        BeanCreateContext beanCreateContext = applicationContextUtil.getBeanCreateContext();
        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();
        EventPublisherContext eventPublisherContext = applicationContextUtil.getEventPublisherContext();
        loadAnnotationHandlerAndApplicationListener(annotationContext, beanCreateContext, beanDefinitionContext, eventPublisherContext);

        //## 遍历剩下的的beanDefinition，并且初始化
        Collection<BeanDefinition> allBeanDefintion = beanDefinitionContext.getAllBeanDefintion();
        CopyOnWriteArrayList<BeanDefinition> copyOnWriteArrayList = new CopyOnWriteArrayList<>(allBeanDefintion);
        for (BeanDefinition beanDefinition : copyOnWriteArrayList) {
            beanCreateContext.createBean(beanDefinition);
        }
        log.info("初始化所有类完成..........................");
        ApplicationEvent applicationEvent3 = new ApplicationEvent("createBean");
        eventPublisherContext.publisherEvent(applicationEvent3);

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
        ApplicationEvent applicationEvent1 = new ApplicationEvent("annotationHandler");
        eventPublisherContext.publisherEvent(applicationEvent1);

        //2. 初始化相关的ApplicationListener
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
        ApplicationEvent applicationEvent2 = new ApplicationEvent("applicationListener");
        eventPublisherContext.publisherEvent(applicationEvent2);
    }

    /**
     * 先初始化系统中的annotationHandler类，并将注解处理器全部放入annotationContext中.
     *
     * @param applicationContextUtil
     */
    private static void loadAnnotationHandler(ApplicationContextUtil applicationContextUtil) {
        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();
        AnnotationContext annotationContext = applicationContextUtil.getAnnotationContext();

        //Component注解
        ComponentAnnotationHandler componentAnnotationHandler = ReflectUtil.newInstanceIfPossible(ComponentAnnotationHandler.class);
        componentAnnotationHandler.setApplicationContextUtil(applicationContextUtil);
        BeanDefinition beanDefinition = newInstance(ComponentAnnotationHandler.class, componentAnnotationHandler);
        beanDefinitionContext.addBeanDefinition(beanDefinition.getBeanName(), beanDefinition.getBeanClass(), beanDefinition);
        annotationContext.addAnnotationHandler(componentAnnotationHandler);

        //Configuration注解
        ConfigurationAnnotationHandler configurationAnnotationHandler = ReflectUtil.newInstanceIfPossible(ConfigurationAnnotationHandler.class);
        configurationAnnotationHandler.setApplicationContextUtil(applicationContextUtil);
        BeanDefinition beanDefinition2 = newInstance(ConfigurationAnnotationHandler.class, configurationAnnotationHandler);
        beanDefinitionContext.addBeanDefinition(beanDefinition2.getBeanName(), beanDefinition2.getBeanClass(), beanDefinition2);
        annotationContext.addAnnotationHandler(configurationAnnotationHandler);

        //InjectAnnotationHandler注解
        InjectAnnotationHandler injectAnnotationHandler = ReflectUtil.newInstanceIfPossible(InjectAnnotationHandler.class);
        injectAnnotationHandler.setApplicationContextUtil(applicationContextUtil);
        BeanDefinition beanDefinition3 = newInstance(InjectAnnotationHandler.class, injectAnnotationHandler);
        beanDefinitionContext.addBeanDefinition(beanDefinition3.getBeanName(), beanDefinition3.getBeanClass(), beanDefinition3);
        annotationContext.addAnnotationHandler(injectAnnotationHandler);

        //ValueAnnotationHandler注解
        ValueAnnotationHandler valueAnnotationHandler = ReflectUtil.newInstanceIfPossible(ValueAnnotationHandler.class);
        valueAnnotationHandler.setPropertiesContext(applicationContextUtil.getPropertiesContext());
        BeanDefinition beanDefinition4 = newInstance(ValueAnnotationHandler.class, valueAnnotationHandler);
        beanDefinitionContext.addBeanDefinition(beanDefinition4.getBeanName(), beanDefinition4.getBeanClass(), beanDefinition4);
        annotationContext.addAnnotationHandler(valueAnnotationHandler);

        //InitAnnotationHandler注解
        InitAnnotationHandler initAnnotationHandler = ReflectUtil.newInstanceIfPossible(InitAnnotationHandler.class);
        BeanDefinition beanDefinition5 = newInstance(InitAnnotationHandler.class, initAnnotationHandler);
        beanDefinitionContext.addBeanDefinition(beanDefinition5.getBeanName(), beanDefinition5.getBeanClass(), beanDefinition5);
        annotationContext.addAnnotationHandler(initAnnotationHandler);

        //AopAnnotationHandler注解
        AopAnnotationHandler aopAnnotationHandler = ReflectUtil.newInstanceIfPossible(AopAnnotationHandler.class);
        aopAnnotationHandler.setBeanCreateContext(applicationContextUtil.getBeanCreateContext());
        aopAnnotationHandler.setBeanDefinitionContext(applicationContextUtil.getBeanDefinitionContext());
        BeanDefinition beanDefinition8 = newInstance(AopAnnotationHandler.class, aopAnnotationHandler);
        beanDefinitionContext.addBeanDefinition(beanDefinition8.getBeanName(), beanDefinition8.getBeanClass(), beanDefinition8);
        annotationContext.addAnnotationHandler(aopAnnotationHandler);
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
