package xyz.xiezc.ioc.common;


import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import lombok.Data;
import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Bean;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.common.event.Event;
import xyz.xiezc.ioc.common.event.EventListenerUtil;
import xyz.xiezc.ioc.common.event.Listener;
import xyz.xiezc.ioc.definition.AnnotationAndHandler;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * bean 类扫描加载工具
 */

public class BeanScanUtil {

    /**
     * 容器
     */
    ContextUtil contextUtil;
    /**
     * 注解工具类
     */
    AnnoUtil annoUtil;

    /**
     * 事件分发处理器
     */
    EventListenerUtil eventListenerUtil;


    public BeanScanUtil(ContextUtil contextUtil, EventListenerUtil eventListenerUtil) {
        this.contextUtil = contextUtil;
        this.annoUtil = contextUtil.getAnnoUtil();
        this.eventListenerUtil = eventListenerUtil;
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
            Component component = AnnotationUtil.getAnnotation(aClass, Component.class);
            if (component != null) {
                AnnotationHandler annotationHandler = annoUtil.getClassAnnoAndHandlerMap().get(Component.class);
                annotationHandler.processClass(component, aClass, contextUtil);
            }

            Configuration configuration = AnnotationUtil.getAnnotation(aClass, Configuration.class);
            if (configuration != null) {
                AnnotationHandler annotationHandler = annoUtil.getClassAnnoAndHandlerMap().get(Configuration.class);
                annotationHandler.processClass(configuration, aClass, contextUtil);
            }
        }
    }

    /**
     * 开始开始初始化bean 并且 注入依赖
     */
    public void initAndInjectBeans() {
        for (BeanDefinition beanDefinition : contextUtil.context) {
            XiocUtil.createBean(beanDefinition, contextUtil);
        }
    }

    /**
     * 扫描所有容器中的类的注解，并处理
     */
    public void scanBeanClass() {
        Map<Class<? extends Annotation>, AnnotationHandler> classAnnoAndHandlerMap = annoUtil.classAnnoAndHandlerMap;

        CopyOnWriteArraySet<BeanDefinition> copyOnWriteArraySet = new CopyOnWriteArraySet(contextUtil.context);
        for (BeanDefinition beanDefinition : copyOnWriteArraySet) {
            Class cla = beanDefinition.getBeanClass();

            //获取这个类上所有的注解
            Annotation[] annotations = AnnotationUtil.getAnnotations(cla, true);
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
    public void scanBeanField() {

        Map<Class<? extends Annotation>, AnnotationHandler> fieldAnnoAndHandlerMap = annoUtil.getFieldAnnoAndHandlerMap();

        CopyOnWriteArraySet<BeanDefinition> copyOnWriteArraySet = new CopyOnWriteArraySet(contextUtil.context);
        for (BeanDefinition beanDefinition : copyOnWriteArraySet) {

            Class<?> tClass = beanDefinition.getBeanClass();
            //获取所有的字段， 包含父类的字段
            Field[] fields = ReflectUtil.getFields(tClass);
            //检查每个字段的注解
            for (Field field : fields) {
                Annotation[] annotations = AnnotationUtil.getAnnotations(field, true);
                if (annotations == null || annotations.length == 0) {
                    continue;
                }
                List<AnnotationAndHandler> collect = CollUtil.newArrayList(annotations).stream()
                        .filter(annotation -> annotation.annotationType() != Inject.class)
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
                    annotationHandler.processField(field, annotation, beanDefinition, contextUtil);
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
    public void scanMethod() {
        Map<Class<? extends Annotation>, AnnotationHandler> methodAnnoAndHandlerMap = annoUtil.getMethodAnnoAndHandlerMap();

        CopyOnWriteArraySet<BeanDefinition> copyOnWriteArraySet = new CopyOnWriteArraySet(contextUtil.context);
        for (BeanDefinition beanDefinition : copyOnWriteArraySet) {
            Class<?> tClass = beanDefinition.getBeanClass();
            Method[] methods = ClassUtil.getDeclaredMethods(tClass);
            //检查每个字段的注解
            for (Method method : methods) {
                Annotation[] annotations = AnnotationUtil.getAnnotations(method, true);
                if (annotations == null || annotations.length == 0) {
                    continue;
                }
                List<AnnotationAndHandler> collect = CollUtil.newArrayList(annotations).stream()
                        .filter(annotation -> annotation.annotationType() != Bean.class)
                        .map(annotation -> {
                            AnnotationHandler annotationHandler = methodAnnoAndHandlerMap.get(annotation.annotationType());
                            AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                            annotationAndHandler.setAnnotation(annotation);
                            annotationAndHandler.setAnnotationHandler(annotationHandler);
                            return annotationAndHandler;
                        })
                        .filter(annotationAndHandler -> annotationAndHandler.getAnnotationHandler() != null)
                        // .sorted(Comparator.comparing(AnnotationAndHandler::getAnnotationHandler))
                        .collect(Collectors.toList());
                for (AnnotationAndHandler annotationAndHandler : collect) {
                    AnnotationHandler annotationHandler = annotationAndHandler.getAnnotationHandler();//processField()
                    Annotation annotation = annotationAndHandler.getAnnotation();
                    annotationHandler.processMethod(method, annotation, beanDefinition, contextUtil);
                }
            }
        }
    }


    /**
     * 扫描系统已有的注解类，
     *
     * @return
     */
    public void loadAnnotation() {
        String annoPath = annoUtil.getAnnoPath();
        //扫描出所有的注解处理器
        Set<Class<?>> classes = ClassUtil.scanPackage(annoPath, clazz ->
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
            annoUtil.addAnnotationHandler(annotationHandler);
        });
    }

    /**
     * 加载配置文件
     */
    public void loadPropertie() {
        //读取classpath下的Application.setting，不使用变量
        Setting setting = new Setting("application.setting");
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
        BeanSignature beanSignature = new BeanSignature();
        beanSignature.setBeanClass(Listener.class);
        beanSignature.setBeanName("listener");
        beanSignature.setBeanTypeEnum(BeanTypeEnum.bean);
        List<BeanDefinition> beanDefinitions = contextUtil.getBeanDefinitions(beanSignature);
        beanDefinitions.forEach(beanDefinition -> {
            Listener bean;
            if (beanDefinition.getBeanStatus() != BeanStatusEnum.Completed) {
                bean = (Listener) XiocUtil.createBean(beanDefinition, contextUtil);
            } else {
                bean = beanDefinition.getBean();
            }
            Event event = bean.getEvent();
            eventListenerUtil.addListener(event, bean);
        });

    }
}
