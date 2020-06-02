package xyz.xiezc.ioc.system.annotation;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.system.common.asm.AsmUtil;
import xyz.xiezc.ioc.system.common.context.AnnotationContext;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.system.common.definition.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 注解处理器， 每一个注解都应该对应一个处理器。
 *
 * @author wb-xzc291800
 * @date 2019/04/04 15:01
 */
public abstract class AnnotationHandler<T extends Annotation> extends AnnotationOrder {

    @Override
    public abstract Class<T> getAnnotationType();

    /**
     * 如果是在类注解， 则会调用这个方法处理
     *
     * @param annotation 这个类上的所有注解
     * @param clazz      被注解类的基本信息
     */
    public abstract void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition);

    /**
     * 如果是方法注解，则会调用这个方法来处理
     *
     * @param beanDefinition   被注解的类
     * @param methodDefinition 被注解的方法
     * @param annotation       这个类上的所有注解
     */
    public abstract void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition);


    /**
     * 如果是字段注解，则会调用这个方法来处理
     *
     * @param beanDefinition  被注解的类
     * @param fieldDefinition 被注解的字段
     * @param annotation      这个类上的所有注解
     */
    public abstract void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition);

    /**
     * 获取BeanDefinition的真实类型， 因为有些Bean是通过FactoryBean创建的
     *
     * @param beanDefinition
     * @return
     */
    protected Class<?> getRealBeanClass(BeanDefinition beanDefinition) {
        if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.factoryBean) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            //对应的beanClass需要重新设置
            Type[] genericInterfaces = beanClass.getGenericInterfaces();
            if (genericInterfaces == null) {
                throw new RuntimeException("factoryBean接口必须显示指定泛型");
            }
            for (Type genericInterface : genericInterfaces) {
                //如果得到的泛型类型是 ParameterizedType（参数化类型）类型的话
                if (ClassUtil.isAssignable(ParameterizedType.class, genericInterface.getClass())) {
                    //将类型转换为 参数类型类
                    ParameterizedType ptype = (ParameterizedType) genericInterface;
                    if (ptype.getRawType() == FactoryBean.class) {
                        //得到  泛型类型的实际  类型
                        Type[] iType = ptype.getActualTypeArguments();
                        beanClass = (Class<?>) iType[0];
                    }
                }
            }
            return beanClass;
        }
        return beanDefinition.getBeanClass();
    }


    protected BeanDefinition dealBeanAnnotation(Annotation annotation, Class clazz, ApplicationContextUtil applicationContextUtil) {
        String beanName = AnnotationUtil.getAnnotationValue(clazz, annotation.annotationType(), "value");
        if (StrUtil.isBlank(beanName)) {
            beanName = clazz.getTypeName();
        }
        return dealBeanAnnotation(beanName, clazz, applicationContextUtil);
    }


    protected BeanDefinition dealBeanAnnotation(String beanName, Class clazz, ApplicationContextUtil applicationContextUtil) {
        Class<?> beanClass = clazz;
        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();
        //获取容器中是否存在这个bean
        BeanDefinition beanDefinition = beanDefinitionContext.getBeanDefinition(beanName, beanClass);
        //如果容器中不存在 这个bean。 就要放入
        if (beanDefinition == null) {
            beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClass(beanClass);
            beanDefinition.setBeanName(beanName);
        }
        //获取这个容器中的注解
        if (ClassUtil.isAssignable(FactoryBean.class, beanClass)) {
            beanDefinition.setBeanTypeEnum(BeanTypeEnum.factoryBean);
        } else {
            beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        }

        //获取bean的inject 注入bean的信息
        Field[] fields = ReflectUtil.getFields(beanClass);
        //获取这个bean的所有待注入的信息
        for (Field field : fields) {
            FieldDefinition fieldDefinition = dealFieldDefinition(field, beanDefinition, applicationContextUtil.getAnnotationContext());
            if (fieldDefinition == null) {
                continue;
            }
            Set<FieldDefinition> injectFiledDefinitions = beanDefinition.getFieldDefinitions();
            if (injectFiledDefinitions == null) {
                injectFiledDefinitions = CollUtil.newHashSet();
                beanDefinition.setFieldDefinitions(injectFiledDefinitions);
            }
            injectFiledDefinitions.add(fieldDefinition);
        }

        //获取这个bean中的所有方法
        Method[] methods = ReflectUtil.getMethods(beanClass);
        for (Method method : methods) {
            MethodDefinition methodDefinition = dealMethodDefinition(method, beanDefinition, applicationContextUtil.getAnnotationContext());
            if (methodDefinition == null) {
                continue;
            }
            Set<MethodDefinition> methodDefinitions = beanDefinition.getMethodDefinitions();
            if (methodDefinitions == null) {
                methodDefinitions = CollUtil.newHashSet();
                beanDefinition.setMethodDefinitions(methodDefinitions);
            }
            methodDefinitions.add(methodDefinition);
        }

        return beanDefinition;
    }


    protected FieldDefinition dealFieldDefinition(Field field, BeanDefinition beanDefinition, AnnotationContext annotationContext) {
        //获取字段上的所有注解
        Annotation[] annotations = AnnotationUtil.getAnnotations(field, true);
        //校验排除一些注解
        List<Annotation> collect = CollUtil.newArrayList(annotations)
                .stream()
                .filter(annotation -> !annotationContext.isNotHandleAnnotation(annotation.annotationType()))
                .filter(annotation -> annotationContext.getFieldAnnotationHandler(annotation.annotationType()) != null)
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(collect)) {
            FieldDefinition fieldDefinition = new FieldDefinition();
            fieldDefinition.setBeanDefinition(beanDefinition);
            fieldDefinition.setFieldName(field.getName());
            fieldDefinition.setFieldType(field.getType());
            fieldDefinition.setField(field);
            return fieldDefinition;
        }
        return null;
    }


    protected MethodDefinition dealMethodDefinition(Method method, BeanDefinition beanDefinition, AnnotationContext annotationContext) {
        //获取字段上的所有注解
        Annotation[] annotations = cn.hutool.core.annotation.AnnotationUtil.getAnnotations(method, true);
        //校验排除一些注解
        List<Annotation> collect = CollUtil.newArrayList(annotations)
                .stream()
                .filter(annotation ->
                        !annotationContext.isNotHandleAnnotation(annotation.annotationType())
                ).filter(annotation -> annotationContext.getMethodAnnotationHandler(annotation.annotationType()) != null)
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(collect)) {
            MethodDefinition methodDefinition = new MethodDefinition();
            methodDefinition.setBeanDefinition(beanDefinition);
            methodDefinition.setMethod(method);
            methodDefinition.setReturnType(method.getReturnType());
            ParamDefinition[] methodParamsAndAnnotaton = AsmUtil.getMethodParamsAndAnnotaton(method);
            methodDefinition.setParamDefinitions(methodParamsAndAnnotaton);
            return methodDefinition;
        }
        return null;

    }

}
