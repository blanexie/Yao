package xyz.xiezc.ioc;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import lombok.Data;
import xyz.xiezc.ioc.annotation.*;
import xyz.xiezc.ioc.common.context.*;
import xyz.xiezc.ioc.common.context.impl.*;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.definition.AnnotationAndHandler;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;
import xyz.xiezc.ioc.enums.EventNameConstant;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static xyz.xiezc.ioc.enums.EventNameConstant.loadBeanDefinition;

/**
 * 容器，装载bean的容器,以及其他的所有的需要存放的对象都在这里
 */
@Data
public class ApplicationContextUtil {

    Log log = LogFactory.get(ApplicationContextUtil.class);

    /**
     * 全局线程池
     */
    public static ExecutorService executorService = ThreadUtil.newExecutor(1, 100);

    private BeanDefinitionContext beanDefinitionContext = new BeanDefinitionContextUtil();
    private AnnotationContext annotationContext = new AnnotationContextUtil();
    private PropertiesContext propertiesContext = new PropertiesContextUtil();
    private EventPublisherContext eventPublisherContext = new EventPublisherContextUtil();

    /**
     *
     */
    private BeanCreateContext beanCreateContext = new BeanCreateContextUtil(beanDefinitionContext, propertiesContext);

    /**
     * 加载其他starter需要扫描的package路径
     */
    public final String starterPackage = "xyz.xiezc.ioc.starter";

    /**
     * 扫描本项目和所有starter中的所有容器类
     *
     * @param clazz
     */
    public void scanBeanDefinitions(Class<?> clazz) {
        //加载框架中的bean
        this.loadBeanDefinitions(ClassUtil.getPackage(clazz));
        //加载starter中的bean
        this.loadBeanDefinitions(starterPackage);
    }

    /**
     * 从容器中获取beanCreate创建类, 并初始化
     */
    public void loadBeanCreateStategy() {
        List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(BeanCreateStrategy.class);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanDefinition = beanCreateContext.newInstance(beanDefinition);
            BeanCreateStrategy bean = beanDefinition.getBean();
            beanCreateContext.putBeanCreateStrategy(bean);
        }
    }

    /**
     * 从容器中加载注解处理类， 并初始化
     *
     * @return
     */
    public void loadAnnotationHandler() {
        List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(AnnotationHandler.class);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanCreateContext.newInstance(beanDefinition);
            annotationContext.addAnnotationHandler(beanDefinition.getBean());
        }
    }


    /**
     * 扫描所有容器中的类的注解，并处理
     */
    public void scanBeanDefinitionClass() {
        //遍历beanDefinition
        Collection<BeanDefinition> values = beanDefinitionContext.getAllBeanDefintion();
        CopyOnWriteArrayList<BeanDefinition> copyOnWriteArrayList = new CopyOnWriteArrayList<>(values);
        for (BeanDefinition beanDefinition : copyOnWriteArrayList) {
            dealClassAnnotation(beanDefinition);
            dealFieldAnnotation(beanDefinition);
            dealMethodAnnotation(beanDefinition);
        }
    }

    /**
     * 处理bean的内伤的注解
     *
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
                    AnnotationHandler annotationHandler = annotationContext.getClassAnnotationHandler(annotation.annotationType());
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
            annotationHandler.processClass(annotation, cla);
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
                        AnnotationHandler annotationHandler = annotationContext.getFieldAnnotationHandler(annotation.annotationType());
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
                annotationHandler.processField(fieldDefinition, annotation, beanDefinition);
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
                        AnnotationHandler annotationHandler = annotationContext.getMethodAnnotationHandler(annotation.annotationType());
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
                annotationHandler.processMethod(methodDefinition, annotation, beanDefinition);
            }
        }
    }


    /**
     * 从容器中获取事件处理器， 并初始化
     */
    public void loadEventListener() {
        List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(ApplicationListener.class);
        beanDefinitions.forEach(beanDefinition -> {
            EventListener annotation = AnnotationUtil.getAnnotation(beanDefinition.getAnnotatedElement(), EventListener.class);
            if (annotation == null) {
                return;
            }
            String[] strings = annotation.eventName();
            beanCreateContext.newInstance(beanDefinition);
            ApplicationListener applicationListener = beanDefinition.getBean();
            for (String eventName : strings) {
                eventPublisherContext.addApplicationListener(eventName, applicationListener);
            }
        });
    }

    public void loadBeanDefinitions(String packagePath) {
        Set<Class<?>> classes = ClassUtil.scanPackage(packagePath);
        for (Class<?> aClass : classes) {
            this.loadBeanDefinition(aClass);
        }
    }


    /**
     * 加载某个具体的类BeanDefinition到容器中， 前提是这个类必须被@Component 和 Configuration注解
     */
    private void loadBeanDefinition(Class clazz) {

        //获取上面的component 注解
        Component component = AnnotationUtil.getAnnotation(clazz, Component.class);
        if (component != null) {
            AnnotationHandler annotationHandler = annotationContext.getClassAnnotationHandler(Component.class);
            if (annotationHandler == null) {
                Class<? extends AnnotationHandler> annotatonHandler = component.annotatonHandler();
                annotationHandler = ReflectUtil.newInstanceIfPossible(annotatonHandler);
                annotationContext.addAnnotationHandler(annotationHandler);
            }
            annotationHandler.processClass(component, clazz);
        }

        Configuration configuration = AnnotationUtil.getAnnotation(clazz, Configuration.class);
        if (configuration != null) {
            AnnotationHandler annotationHandler = annotationContext.getClassAnnotationHandler(Configuration.class);
            if (annotationHandler == null) {
                Class<? extends AnnotationHandler> annotatonHandler = configuration.annotatonHandler();
                annotationHandler = ReflectUtil.newInstanceIfPossible(annotatonHandler);
                annotationContext.addAnnotationHandler(annotationHandler);
            }
            annotationHandler.processClass(configuration, clazz);
        }
    }


}
