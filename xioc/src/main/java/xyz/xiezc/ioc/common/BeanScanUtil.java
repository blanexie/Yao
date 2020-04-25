package xyz.xiezc.ioc.common;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.create.BeanFactoryUtil;
import xyz.xiezc.ioc.common.create.FactoryBeanCreateStrategy;
import xyz.xiezc.ioc.common.create.MethodBeanCreateStrategy;
import xyz.xiezc.ioc.common.event.Event;
import xyz.xiezc.ioc.common.event.EventListenerUtil;
import xyz.xiezc.ioc.common.event.Listener;
import xyz.xiezc.ioc.definition.*;
import xyz.xiezc.ioc.enums.BeanStatusEnum;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * bean 类扫描加载工具
 */

public class BeanScanUtil {

    /**
     * 容器
     */
    ContextUtil contextUtil;


    BeanFactoryUtil beanFactoryUtil;

    /**
     * 注解工具类
     */
    AnnotationUtil annotationUtil;

    /**
     * 事件分发处理器
     */
    EventListenerUtil eventListenerUtil;


    public BeanScanUtil(ContextUtil contextUtil, EventListenerUtil eventListenerUtil) {
        this.contextUtil = contextUtil;
        this.annotationUtil = contextUtil.getAnnotationUtil();
        this.eventListenerUtil = eventListenerUtil;
    }

    /**
     * 加载bean初始化类
     */
    public void loadBeanCreateStategy() {
        beanFactoryUtil = new BeanFactoryUtil();
        beanFactoryUtil.setContextUtil(contextUtil);
        beanFactoryUtil.addBeanFactoryStrategy(new MethodBeanCreateStrategy());
        beanFactoryUtil.addBeanFactoryStrategy(new FactoryBeanCreateStrategy());
        beanFactoryUtil.addBeanFactoryStrategy(new BeanCreateStrategy());
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
                AnnotationHandler annotationHandler = annotationUtil.getClassAnnoAndHandlerMap().get(Component.class);
                annotationHandler.processClass(component, aClass, contextUtil);
            }

            Configuration configuration = cn.hutool.core.annotation.AnnotationUtil.getAnnotation(aClass, Configuration.class);
            if (configuration != null) {
                AnnotationHandler annotationHandler = annotationUtil.getClassAnnoAndHandlerMap().get(Configuration.class);
                annotationHandler.processClass(configuration, aClass, contextUtil);
            }
        }
    }

    /**
     * 开始开始初始化bean 并且 注入依赖
     */
    public void initAndInjectBeans() {
        contextUtil.classaAndBeanDefinitionMap.forEach((clazz, beanDefinition) -> {
            beanFactoryUtil.createBean(beanDefinition);
        });
    }

    /**
     * 扫描所有容器中的类的注解，并处理
     */
    public void scanBeanDefinitionClass() {
        Map<Class<? extends Annotation>, AnnotationHandler> classAnnoAndHandlerMap = annotationUtil.classAnnoAndHandlerMap;
        //遍历beanDefinition
        Collection<BeanDefinition> values = contextUtil.classaAndBeanDefinitionMap.values();
        CopyOnWriteArrayList<BeanDefinition> copyOnWriteArrayList = new CopyOnWriteArrayList<>(values);
        for (BeanDefinition beanDefinition : copyOnWriteArrayList) {
            Class cla = beanDefinition.getBeanClass();
            AnnotatedElement annotatedElement = beanDefinition.getAnnotatedElement();

            //获取这个类上所有的注解
            Annotation[] annotations = cn.hutool.core.annotation.AnnotationUtil.getAnnotations(annotatedElement, true);
            //获取注解和handler
            List<AnnotationAndHandler> collect = CollUtil.newArrayList(annotations)
                    .stream()
                    .filter(annotation -> {
                        Class<? extends Annotation> aClass = annotation.annotationType();
                        boolean isBeanAnno = Component.class == aClass || Configuration.class == aClass;
                        return !isBeanAnno;
                    })
                    .map(annotation -> {
                        AnnotationHandler annotationHandler = classAnnoAndHandlerMap.get(annotation.annotationType());
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
                annotationHandler.processClass(annotation, cla, contextUtil);
            }
        }
    }


    /**
     * 扫描bean类中字段的注解
     *
     * @return
     */
    public void scanBeanDefinitionField() {
        Map<Class<? extends Annotation>, AnnotationHandler> fieldAnnoAndHandlerMap = annotationUtil.getFieldAnnoAndHandlerMap();
        //遍历beanDefinition
        Collection<BeanDefinition> values = contextUtil.classaAndBeanDefinitionMap.values();
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
                            AnnotationHandler annotationHandler = fieldAnnoAndHandlerMap.get(annotation.annotationType());
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
                    annotationHandler.processField(fieldDefinition, annotation, beanDefinition, contextUtil);
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
        Map<Class<? extends Annotation>, AnnotationHandler> methodAnnoAndHandlerMap = annotationUtil.getMethodAnnoAndHandlerMap();

        Collection<BeanDefinition> values = contextUtil.classaAndBeanDefinitionMap.values();
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
                            AnnotationHandler annotationHandler = methodAnnoAndHandlerMap.get(annotation.annotationType());
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
                    annotationHandler.processMethod(methodDefinition, annotation, beanDefinition, contextUtil);
                }
            }
        }
    }


    /**
     * 扫描系统已有的注解类，
     *
     * @return
     */
    public void loadAnnotationHandler(String annoPath) {
        //扫描出所有的注解处理器
        Set<Class<?>> classes = ClassUtil
                .scanPackage(annoPath, clazz ->
                        AnnotationHandler.class.isAssignableFrom(clazz)
                );
        //反射生成所有的注解处理器
        Set<AnnotationHandler> annotationHandlerSet = new HashSet<>();
        for (Class<?> aClass : classes) {
            try {
                AnnotationHandler annotationHandler = (AnnotationHandler) ReflectUtil.getConstructor(aClass).newInstance();
                annotationHandlerSet.add(annotationHandler);
            } catch (InstantiationException e) {
                ExceptionUtil.wrapAndThrow(e);
            } catch (IllegalAccessException e) {
                ExceptionUtil.wrapAndThrow(e);
            } catch (InvocationTargetException e) {
                ExceptionUtil.wrapAndThrow(e);
            }
        }
        //分类处理注解处理器
        annotationHandlerSet.forEach(annotationHandler -> {
            annotationUtil.addAnnotationHandler(annotationHandler);
        });
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
        contextUtil.setSetting(setting);
    }

    /**
     * 获取容器中的事件监听器， 这个方法是在。 初始化完成后执行
     */
    public void loadEventListener() {
        List<BeanDefinition> beanDefinitions = contextUtil.getBeanDefinitions(Listener.class);
        beanDefinitions.forEach(beanDefinition -> {
            beanFactoryUtil.createBean(beanDefinition);
            Listener bean = beanDefinition.getBean();
            Event event = bean.getEvent();
            eventListenerUtil.addListener(event, bean);
        });

    }
}
