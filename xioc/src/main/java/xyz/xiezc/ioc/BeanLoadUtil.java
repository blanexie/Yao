package xyz.xiezc.ioc;


import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import lombok.SneakyThrows;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xyz.xiezc.ioc.enums.EventNameConstant.loadPropertie;

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
                Class<? extends AnnotationHandler> annotatonHandler = Component.annotatonHandler;
                annotationHandler = ReflectUtil.newInstanceIfPossible(annotatonHandler);
                applicationContextUtil.addAnnotationHandler(annotationHandler);
            }
            annotationHandler.processClass(component, clazz, applicationContextUtil);
        }

        Configuration configuration = AnnotationUtil.getAnnotation(clazz, Configuration.class);
        if (configuration != null) {
            AnnotationHandler annotationHandler = applicationContextUtil.getClassAnnotationHandler(Configuration.class);
            if (annotationHandler == null) {
                Class<? extends AnnotationHandler> annotatonHandler = Configuration.annotatonHandler;
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
        applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.scanBeanDefinitionClass));

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

        applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.scanBeanDefinitionField));

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

        applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.scanBeanDefinitionMethod));

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
     * 框架只认三个配置文件的名称， 如果需要导入其他的配置文件， 需要在这个三个主配置文件中指明
     * 加载配置文件
     */
    public void loadPropertie() {
        //读取classpath下的Application.setting，不使用变量
        //优先加载框架主要的配置文件
        File file1 = FileUtil.file("yao.setting");
        if (FileUtil.exist(file1)) {
            applicationContextUtil.addSetting(new Setting(file1.getPath(), true));
        }
        File file2 = FileUtil.file("xioc.setting");
        if (FileUtil.exist(file2)) {
            applicationContextUtil.addSetting(new Setting(file2.getPath(), true));
        }
        File file3 = FileUtil.file("xweb.setting");
        if (FileUtil.exist(file3)) {
            applicationContextUtil.addSetting(new Setting(file3.getPath(), true));
        }
        File file5= FileUtil.file("app.setting");
        if (FileUtil.exist(file5)) {
            applicationContextUtil.addSetting(new Setting(file5.getPath(), true));
        }
        //加载关联的配置文件
        String s = applicationContextUtil.getSetting().get("setting.import.path");
        if (StrUtil.isNotBlank(s)) {
            String[] split = s.split(",");
            for (String s1 : split) {
                File file4 = FileUtil.file(s1);
                if (FileUtil.exist(file4)) {
                    applicationContextUtil.addSetting(new Setting(file4.getPath(), true));
                }
            }
        }
        //发布事件
        applicationContextUtil.publisherEvent(new ApplicationEvent(loadPropertie));
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
