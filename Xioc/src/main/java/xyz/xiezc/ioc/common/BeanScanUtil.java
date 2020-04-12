package xyz.xiezc.ioc.common;


import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.*;
import xyz.xiezc.ioc.definition.*;
import xyz.xiezc.ioc.enums.BeanStatusEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
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


    public BeanScanUtil(ContextUtil contextUtil) {
        this.contextUtil = contextUtil;
        this.annoUtil = contextUtil.getAnnoUtil();
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
    public void scanClass() {
        Map<Class<? extends Annotation>, AnnotationHandler> classAnnoAndHandlerMap = annoUtil.classAnnoAndHandlerMap;
        //扫描到类
        contextUtil.context.forEach(beanDefinition -> {
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
        });
    }


    /**
     * 扫描bean类中字段的注解
     *
     * @return
     */
    public void scanField() {

        Map<Class<? extends Annotation>, AnnotationHandler> fieldAnnoAndHandlerMap = annoUtil.getFieldAnnoAndHandlerMap();

        contextUtil.context.forEach(beanDefinition -> {
            Class<?> tClass = beanDefinition.getBeanClass();
            //获取所有的字段， 包含父类的字段
            Field[] fields = ReflectUtil.getFields(tClass);
            //检查每个字段的注解
            for (Field field : fields) {
                Annotation[] annotations = AnnotationUtil.getAnnotations(field, true);
                if (annotations == null) {
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
        });
    }

    /**
     * 扫描bean类中方法的注解. 这个在bean都初始化后执行
     *
     * @return
     */
    public void scanMethod() {
        Map<Class<? extends Annotation>, AnnotationHandler> methodAnnoAndHandlerMap = annoUtil.getMethodAnnoAndHandlerMap();

        for (BeanDefinition beanDefinition : contextUtil.context) {
            if (beanDefinition.getBeanStatus() != BeanStatusEnum.Completed) {
                throw new RuntimeException("容器中还存在实例化未完成的bean：{}" + beanDefinition.toString());
            }
            Class<?> tClass = beanDefinition.getBeanClass();
            Method[] methods = ReflectUtil.getMethods(tClass);
            //检查每个字段的注解
            for (Method method : methods) {

                Annotation[] annotations = AnnotationUtil.getAnnotations(method, true);
                if (annotations == null) {
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

}
