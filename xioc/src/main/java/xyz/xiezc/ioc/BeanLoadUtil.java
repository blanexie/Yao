package xyz.xiezc.ioc;


import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.annotation.EventListener;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.common.exception.CircularDependenceException;
import xyz.xiezc.ioc.definition.AnnotationAndHandler;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.EventNameConstant;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * bean 类扫描加载工具
 */

public class BeanLoadUtil {

    Log log = LogFactory.get(BeanLoadUtil.class);

    /**
     * 容器
     */
    ApplicationContextUtil applicationContextUtil;


    public BeanLoadUtil(ApplicationContextUtil applicationContextUtil) {
        this.applicationContextUtil = applicationContextUtil;
    }

    /**
     * 加载bean初始化类
     */
    public void loadBeanCreateStategy() {
        List<BeanDefinition> beanDefinitions = applicationContextUtil.getBeanDefinitions(BeanCreateStrategy.class);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanDefinition = applicationContextUtil.newInstance(beanDefinition);
            BeanCreateStrategy bean = beanDefinition.getBean();
            bean.setApplicationContext(applicationContextUtil);
            applicationContextUtil.putBeanCreateStrategy(bean);
        }
        applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.loadBeanCreateStategy));

    }


    /**
     * 加载所有的bean信息
     *
     * @param packagePath 需要扫描的路径
     */
    public void loadBeanDefinition(String packagePath) {
        //扫描到类
        Set<Class<?>> classes = ClassUtil.scanPackage(packagePath);
        for (Class<?> aClass : classes) {
            this.loadBeanDefinition(aClass);
        }

    }

    /**
     * 加载某个具体的类BeanDefinition到容器中， 前提是这个类必须被@Component 和 Configuration注解
     */
    public void loadBeanDefinition(Class clazz) {
        //获取上面的component 注解
        Component component = AnnotationUtil.getAnnotation(clazz, Component.class);
        if (component != null) {
            AnnotationHandler annotationHandler = applicationContextUtil.getClassAnnotationHandler(Component.class);
            if (annotationHandler == null) {
                Class<? extends AnnotationHandler> annotatonHandler = component.annotatonHandler();
                annotationHandler = ReflectUtil.newInstanceIfPossible(annotatonHandler);
                applicationContextUtil.addAnnotationHandler(annotationHandler);
            }
            annotationHandler.processClass(component, clazz, applicationContextUtil);
        }

        Configuration configuration = AnnotationUtil.getAnnotation(clazz, Configuration.class);
        if (configuration != null) {
            AnnotationHandler annotationHandler = applicationContextUtil.getClassAnnotationHandler(Configuration.class);
            if (annotationHandler == null) {
                Class<? extends AnnotationHandler> annotatonHandler = configuration.annotatonHandler();
                annotationHandler = ReflectUtil.newInstanceIfPossible(annotatonHandler);
                applicationContextUtil.addAnnotationHandler(annotationHandler);
            }
            annotationHandler.processClass(configuration, clazz, applicationContextUtil);
        }
    }

    /**
     * 开始开始初始化bean 并且 注入依赖
     */
    public void initAndInjectBeans() {
        applicationContextUtil.getAllBeanDefintion().forEach(beanDefinition -> {
            try {
                applicationContextUtil.createBean(beanDefinition);
            } catch (CircularDependenceException e1) {
                log.error("初始化" + beanDefinition.toString() + "的时候出现循环依赖");
                log.error(e1);
                throw e1;
            }
        });

        applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.initAndInjectBeans));
    }

    /**
     * 扫描所有容器中的类的注解，并处理
     */
    public void scanBeanDefinitionClass() {
        //遍历beanDefinition
        Collection<BeanDefinition> values = applicationContextUtil.getAllBeanDefintion();
        CopyOnWriteArrayList<BeanDefinition> copyOnWriteArrayList = new CopyOnWriteArrayList<>(values);
        for (BeanDefinition beanDefinition : copyOnWriteArrayList) {
            dealClassAnnotation(beanDefinition);
            dealFieldAnnotation(beanDefinition);
            dealMethodAnnotation(beanDefinition);
        }
        applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.scanBeanDefinitionClass));
    }

    /**
     * 处理bean的内伤的注解
     * @param beanDefinition
     */
    private void dealClassAnnotation(BeanDefinition beanDefinition) {
        Class cla = beanDefinition.getBeanClass();
        AnnotatedElement annotatedElement = beanDefinition.getAnnotatedElement();
        //获取这个类上所有的注解
        Annotation[] annotations = AnnotationUtil.getAnnotations(annotatedElement, true);
        //获取注解和handler
        List<AnnotationAndHandler> collect = CollUtil.newArrayList(annotations)
                .stream()
                .filter(annotation -> {
                    Class<? extends Annotation> aClass = annotation.annotationType();
                    boolean isBeanAnno = Component.class == aClass || Configuration.class == aClass;
                    return !isBeanAnno;
                })
                .map(annotation -> {
                    AnnotationHandler annotationHandler = applicationContextUtil.getClassAnnotationHandler(annotation.annotationType());
                    AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                    annotationAndHandler.setAnnotation(annotation);
                    annotationAndHandler.setAnnotationHandler(annotationHandler);
                    return annotationAndHandler;
                })
                .filter(annotationAndHandler -> annotationAndHandler.getAnnotationHandler() != null)
                .sorted((a, b) -> {
                    AnnotationHandler annotationHandlerA = a.getAnnotationHandler();
                    AnnotationHandler annotationHandlerB = b.getAnnotationHandler();
                    return annotationHandlerA.compareTo(annotationHandlerB);
                })
                .collect(Collectors.toList());

        //遍历排好序的handler， 并且调用处理方法
        for (AnnotationAndHandler annotationAndHandler : collect) {
            AnnotationHandler annotationHandler = annotationAndHandler.getAnnotationHandler();
            Annotation annotation = annotationAndHandler.getAnnotation();
            annotationHandler.processClass(annotation, cla, applicationContextUtil);
        }
    }


    private void dealFieldAnnotation(BeanDefinition beanDefinition) {
        Set<FieldDefinition> annotationFiledDefinitions = beanDefinition.getFieldDefinitions();
        if (annotationFiledDefinitions == null) {
            return;
        }
        //检查每个字段的注解
        for (FieldDefinition fieldDefinition : annotationFiledDefinitions) {
            AnnotatedElement annotatedElement = fieldDefinition.getAnnotatedElement();
            Annotation[] annotations = AnnotationUtil.getAnnotations(annotatedElement, true);
            //遍历注解，找到注解处理器
            List<AnnotationAndHandler> collect = CollUtil.newArrayList(annotations)
                    .stream()
                    .map(annotation -> {
                        AnnotationHandler annotationHandler = applicationContextUtil.getFieldAnnotationHandler(annotation.annotationType());
                        AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                        annotationAndHandler.setAnnotation(annotation);
                        annotationAndHandler.setAnnotationHandler(annotationHandler);
                        return annotationAndHandler;
                    })
                    .filter(annotationAndHandler -> annotationAndHandler.getAnnotationHandler() != null)
                    .sorted((a, b) -> {
                        AnnotationHandler annotationHandlerA = a.getAnnotationHandler();
                        AnnotationHandler annotationHandlerB = b.getAnnotationHandler();
                        return annotationHandlerA.compareTo(annotationHandlerB);
                    })
                    .collect(Collectors.toList());

            for (AnnotationAndHandler annotationAndHandler : collect) {
                AnnotationHandler annotationHandler = annotationAndHandler.getAnnotationHandler();//processField()
                Annotation annotation = annotationAndHandler.getAnnotation();
                annotationHandler.processField(fieldDefinition, annotation, beanDefinition, applicationContextUtil);
            }
        }
    }

    private void dealMethodAnnotation(BeanDefinition beanDefinition) {
        Set<MethodDefinition> annotationMethodDefinitions = beanDefinition.getMethodDefinitions();
        if (annotationMethodDefinitions == null) {
            return;
        }
        //检查每个方法的注解
        for (MethodDefinition methodDefinition : annotationMethodDefinitions) {
            Annotation[] annotations = AnnotationUtil.getAnnotations(methodDefinition.getAnnotatedElement(), true);
            if (annotations == null || annotations.length == 0) {
                continue;
            }
            List<AnnotationAndHandler> collect = CollUtil.newArrayList(annotations)
                    .stream()
                    .map(annotation -> {
                        AnnotationHandler annotationHandler = applicationContextUtil.getMethodAnnotationHandler(annotation.annotationType());
                        AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                        annotationAndHandler.setAnnotation(annotation);
                        annotationAndHandler.setAnnotationHandler(annotationHandler);
                        return annotationAndHandler;
                    })
                    .filter(annotationAndHandler -> annotationAndHandler.getAnnotationHandler() != null)
                    .sorted((a, b) -> {
                        int orderA = a.getAnnotationHandler().getOrder();
                        int orderB = b.getAnnotationHandler().getOrder();
                        return orderA > orderB ? 1 : -1;
                    })
                    .collect(Collectors.toList());

            for (AnnotationAndHandler annotationAndHandler : collect) {
                AnnotationHandler annotationHandler = annotationAndHandler.getAnnotationHandler();
                Annotation annotation = annotationAndHandler.getAnnotation();
                annotationHandler.processMethod(methodDefinition, annotation, beanDefinition, applicationContextUtil);
            }
        }
    }


    /**
     * 扫描系统已有的注解类，
     *
     * @return
     */
    public void loadAnnotationHandler() {
        List<BeanDefinition> beanDefinitions = applicationContextUtil.getBeanDefinitions(AnnotationHandler.class);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            applicationContextUtil.newInstance(beanDefinition);
            applicationContextUtil.addAnnotationHandler(beanDefinition.getBean());
        }
        applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.loadAnnotationHandler));
    }


    /**
     * 获取容器中的事件监听器， 这个方法是在。 初始化完成后执行
     */
    public void loadEventListener() {
        List<BeanDefinition> beanDefinitions = applicationContextUtil.getBeanDefinitions(ApplicationListener.class);
        beanDefinitions.forEach(beanDefinition -> {
            EventListener annotation = AnnotationUtil.getAnnotation(beanDefinition.getAnnotatedElement(), EventListener.class);
            if (annotation == null) {
                return;
            }
            String[] strings = annotation.eventName();
            applicationContextUtil.newInstance(beanDefinition);
            ApplicationListener applicationListener = beanDefinition.getBean();
            for (String eventName : strings) {
                applicationContextUtil.addApplicationListener(eventName, applicationListener);
            }
        });

        applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.loadEventListener));
    }
}
