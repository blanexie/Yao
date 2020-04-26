package xyz.xiezc.ioc;


import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.common.create.*;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.EventListenerUtil;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.definition.*;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * bean 类扫描加载工具
 */

public class BeanStartUtil {

    /**
     * 容器
     */
    ApplicationContextUtil applicationContextUtil;


    BeanCreateUtil beanCreateUtil;

    /**
     * 事件分发处理器
     */
    EventListenerUtil eventListenerUtil;


    public BeanStartUtil(ApplicationContextUtil applicationContextUtil, EventListenerUtil eventListenerUtil) {
        this.applicationContextUtil = applicationContextUtil;
        this.eventListenerUtil = eventListenerUtil;
    }

    /**
     * 加载bean初始化类
     */
    public void loadBeanCreateStategy() {
        List<BeanDefinition> beanDefinitions = applicationContextUtil.getBeanDefinitions(BeanCreateStrategy.class);
        beanCreateUtil.setApplicationContextUtil(applicationContextUtil);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanDefinition = beanCreateUtil.newInstance(beanDefinition);
            beanCreateUtil.addBeanFactoryStrategy(beanDefinition.getBean());
        }
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
            //获取上面的component 注解
            Component component = cn.hutool.core.annotation.AnnotationUtil.getAnnotation(aClass, Component.class);
            if (component != null) {
                AnnotationHandler annotationHandler = applicationContextUtil.getClassAnnotationHandler(Component.class);
                annotationHandler.processClass(component, aClass, applicationContextUtil);
            }

            Configuration configuration = cn.hutool.core.annotation.AnnotationUtil.getAnnotation(aClass, Configuration.class);
            if (configuration != null) {
                AnnotationHandler annotationHandler = applicationContextUtil.getClassAnnotationHandler(Configuration.class);
                annotationHandler.processClass(configuration, aClass, applicationContextUtil);
            }
        }
    }

    /**
     * 开始开始初始化bean 并且 注入依赖
     */
    public void initAndInjectBeans() {

        applicationContextUtil.getAllBeanDefintion().forEach(beanDefinition -> {
            beanCreateUtil.createBean(beanDefinition);
        });
    }

    /**
     * 扫描所有容器中的类的注解，并处理
     */
    public void scanBeanDefinitionClass() {
        //遍历beanDefinition
        Collection<BeanDefinition> values = applicationContextUtil.getAllBeanDefintion();
        CopyOnWriteArrayList<BeanDefinition> copyOnWriteArrayList = new CopyOnWriteArrayList<>(values);
        for (BeanDefinition beanDefinition : copyOnWriteArrayList) {
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
    }


    /**
     * 扫描bean类中字段的注解
     *
     * @return
     */
    public void scanBeanDefinitionField() {
        //遍历beanDefinition
        Collection<BeanDefinition> values = applicationContextUtil.getAllBeanDefintion();
        CopyOnWriteArrayList<BeanDefinition> copyOnWriteArrayList = new CopyOnWriteArrayList<>(values);
        for (BeanDefinition beanDefinition : copyOnWriteArrayList) {
            Set<FieldDefinition> annotationFiledDefinitions = beanDefinition.getAnnotationFiledDefinitions();
            if (annotationFiledDefinitions == null) {
                continue;
            }
            //检查每个字段的注解
            for (FieldDefinition fieldDefinition : annotationFiledDefinitions) {
                AnnotatedElement annotatedElement = fieldDefinition.getAnnotatedElement();
                Annotation[] annotations = cn.hutool.core.annotation.AnnotationUtil.getAnnotations(annotatedElement, true);
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
    }

    /**
     * 扫描bean类中方法的注解. 这个在bean都初始化后执行
     *
     * @return
     */
    @SneakyThrows
    public void scanBeanDefinitionMethod() {

        Collection<BeanDefinition> values = applicationContextUtil.getAllBeanDefintion();
        CopyOnWriteArrayList<BeanDefinition> copyOnWriteArrayList = new CopyOnWriteArrayList<>(values);
        for (BeanDefinition beanDefinition : copyOnWriteArrayList) {
            Set<MethodDefinition> annotationMethodDefinitions = beanDefinition.getAnnotationMethodDefinitions();
            if (annotationMethodDefinitions == null) {
                continue;
            }
            //检查每个字段的注解
            for (MethodDefinition methodDefinition : annotationMethodDefinitions) {
                Annotation[] annotations = cn.hutool.core.annotation.AnnotationUtil.getAnnotations(methodDefinition.getAnnotatedElement(), true);
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
    }


    /**
     * 扫描系统已有的注解类，
     *
     * @return
     */
    public void loadAnnotationHandler() {
        List<BeanDefinition> beanDefinitions = applicationContextUtil.getBeanDefinitions(AnnotationHandler.class);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanCreateUtil.newInstance(beanDefinition);
            applicationContextUtil.addAnnotationHandler(beanDefinition.getBean());
        }

    }

    /**
     * 加载配置文件
     */
    public void loadPropertie() {
        //读取classpath下的Application.setting，不使用变量
        File file = FileUtil.file("application.setting");
        Setting setting;
        if (file.exists()) {
            setting = new Setting("application.setting");
        } else {
            setting = new Setting();
        }
        String str = setting.getStr("other.setting.path");
        String[] split = StrUtil.split(str, ",");
        for (String s : split) {
            Setting setting1 = new Setting(s);
            setting.addSetting(setting1);
        }
        //配置文件的设置
        applicationContextUtil.addSetting(setting);
    }

    /**
     * 获取容器中的事件监听器， 这个方法是在。 初始化完成后执行
     */
    public void loadEventListener() {
        List<BeanDefinition> beanDefinitions = applicationContextUtil.getBeanDefinitions(ApplicationListener.class);
        beanDefinitions.forEach(beanDefinition -> {
            beanCreateUtil.createBean(beanDefinition);
            ApplicationListener bean = beanDefinition.getBean();
            ApplicationEvent applicationEvent = bean.getEvent();
            eventListenerUtil.addListener(applicationEvent, bean);
        });

    }
}