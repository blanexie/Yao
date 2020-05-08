package xyz.xiezc.ioc.annotation.handler;


import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.definition.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ComponentAnnotationHandler extends AnnotationHandler<Component> {

    @Override
    public Class<Component> getAnnotationType() {
        return Component.class;
    }

    /**
     * 这个注解的左右就是把bean放入容器中
     *
     * @param clazz       被注解的类
     * @param annotation  注解
     * @param contextUtil 容器中已有的所有bean信息
     */
    @Override
    public void processClass(Component annotation, Class clazz, ApplicationContextUtil contextUtil) {
        BeanDefinition beanDefinition =dealBeanAnnotation(annotation, clazz, contextUtil);
        Class<?> beanClass =getRealBeanClass(beanDefinition);
        contextUtil.addBeanDefinition(beanDefinition.getBeanName(), beanClass, beanDefinition);
        //扫描这个bean的类上的其他注解，并处理
        dealClassAnnotation(beanDefinition, contextUtil);
        //扫描这个bean的方法上的其他注解，并处理
        dealMethodAnnotation(beanDefinition, contextUtil);
        //扫描这个bean上的所有字段的注解
        dealFieldAnnotation(beanDefinition, contextUtil);

    }



    private void dealFieldAnnotation(BeanDefinition beanDefinition, ApplicationContextUtil applicationContextUtil) {
        Set<FieldDefinition> annotationFiledDefinitions = beanDefinition.getAnnotationFiledDefinitions();
        if (annotationFiledDefinitions == null) {
            return;
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
                        if (annotationHandler == null) {
                            Class<?> aClass = AnnotationUtil.getAnnotationValue(fieldDefinition.getAnnotatedElement(), annotation.annotationType(), "annotatonHandler");
                            if (aClass == null) {
                                return null;
                            } else {
                                BeanDefinition beanDefinitionAnnotation = dealBeanAnnotation(aClass.getName(), aClass, applicationContextUtil);
                                applicationContextUtil.addBeanDefinition(aClass.getName(), aClass, beanDefinitionAnnotation);
                                applicationContextUtil.newInstance(beanDefinitionAnnotation);
                                annotationHandler = beanDefinitionAnnotation.getBean();
                                applicationContextUtil.addAnnotationHandler(annotationHandler);
                            }
                        }

                        AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                        annotationAndHandler.setAnnotation(annotation);
                        annotationAndHandler.setAnnotationHandler(annotationHandler);
                        return annotationAndHandler;
                    }).filter(Objects::nonNull)
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

    public void dealMethodAnnotation(BeanDefinition beanDefinition, ApplicationContextUtil applicationContextUtil) {
        Set<MethodDefinition> annotationMethodDefinitions = beanDefinition.getAnnotationMethodDefinitions();
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
                        if (annotationHandler == null) {
                            Class<?> aClass = AnnotationUtil.getAnnotationValue(methodDefinition.getMethod(), annotation.annotationType(), "annotatonHandler");
                            if (aClass == null) {
                                return null;
                            } else {
                                BeanDefinition beanDefinitionAnnotation = dealBeanAnnotation(aClass.getName(), aClass, applicationContextUtil);
                                applicationContextUtil.addBeanDefinition(aClass.getName(), aClass, beanDefinitionAnnotation);
                                applicationContextUtil.newInstance(beanDefinitionAnnotation);
                                annotationHandler = beanDefinitionAnnotation.getBean();
                                applicationContextUtil.addAnnotationHandler(annotationHandler);
                            }
                        }
                        AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                        annotationAndHandler.setAnnotation(annotation);
                        annotationAndHandler.setAnnotationHandler(annotationHandler);
                        return annotationAndHandler;
                    })
                    .filter(Objects::nonNull)
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

    private void dealClassAnnotation(BeanDefinition beanDefinition, ApplicationContextUtil applicationContextUtil) {
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
                    if (annotationHandler == null) {
                        Class<?> aClass = AnnotationUtil.getAnnotationValue(cla, annotation.annotationType(), "annotatonHandler");
                        if (aClass == null) {
                            return null;
                        } else {
                            BeanDefinition beanDefinitionAnnotation = dealBeanAnnotation(aClass.getName(), aClass, applicationContextUtil);
                            applicationContextUtil.addBeanDefinition(aClass.getName(), aClass, beanDefinitionAnnotation);
                            applicationContextUtil.newInstance(beanDefinitionAnnotation);
                            annotationHandler = beanDefinitionAnnotation.getBean();
                            applicationContextUtil.addAnnotationHandler(annotationHandler);
                        }
                    }
                    AnnotationAndHandler annotationAndHandler = new AnnotationAndHandler();
                    annotationAndHandler.setAnnotation(annotation);
                    annotationAndHandler.setAnnotationHandler(annotationHandler);
                    return annotationAndHandler;
                })
                .filter(Objects::nonNull)
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

    /**
     * 处理Component 注解的方法
     *
     * @param methodDefinition 被注解的方法
     * @param annotation       这个类上的所有注解
     * @param beanSignature    被注解的类
     * @param contextUtil      容器中已有的所有bean信息
     */
    @Override
    public void processMethod(MethodDefinition methodDefinition, Component annotation, BeanDefinition beanSignature, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Component annotation, BeanDefinition beanSignature, ApplicationContextUtil contextUtil) {

    }


}
