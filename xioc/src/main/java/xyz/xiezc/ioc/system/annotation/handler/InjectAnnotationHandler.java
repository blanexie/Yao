package xyz.xiezc.ioc.system.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.system.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.Inject;
import xyz.xiezc.ioc.system.common.context.BeanCreateContext;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.system.common.enums.FieldOrParamTypeEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Data
public class InjectAnnotationHandler extends AnnotationHandler<Inject> {


    ApplicationContextUtil applicationContextUtil;

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public Class<Inject> getAnnotationType() {
        return Inject.class;
    }

    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }

    /**
     * 这个方法就是设置fieldDefinition中的beanName字段值。
     *
     * @param fieldDefinition 被注解的字段
     * @param annotation      这个类上的所有注解
     * @param beanDefinition  被注解的类
     */
    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {
        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();
        BeanCreateContext beanCreateContext = applicationContextUtil.getBeanCreateContext();

        String beanName = AnnotationUtil.getAnnotationValue((AnnotatedElement) annotation, annotation.annotationType());
        if (StrUtil.isBlank(beanName)) {
            beanName = fieldDefinition.getFieldName();
        }

        Object bean = getBean(beanDefinition);
        Class<?> fieldType = fieldDefinition.getFieldType();
        //如果是数组类型，则获取数组中的真实类型
        if (fieldType.isArray()) {
            Class<?> componentType = fieldType.getComponentType();
            fieldDefinition.setFieldType(componentType);
            fieldDefinition.setFieldOrParamTypeEnum(FieldOrParamTypeEnum.Array);

            List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(componentType);
            List<Object> collect = beanDefinitions.stream().map(beanDefinition1 -> {
                BeanDefinition bean1 = beanCreateContext.createBean(beanDefinition1);
                return bean1.getBean();
            }).collect(Collectors.toList());

            ReflectUtil.setFieldValue(bean, fieldDefinition.getField(), collect.toArray());
        } else if (ClassUtil.isAssignable(Collection.class, fieldType)) { //如果是集合类型， 就获取泛型值
            Field declaredField = ClassUtil.getDeclaredField(beanDefinition.getBeanClass(), fieldDefinition.getFieldName());
            Type genericType = declaredField.getGenericType();
            if (genericType != null && genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                //得到泛型里的class类型对象
                Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];
                fieldDefinition.setFieldType(genericClazz);
                fieldDefinition.setFieldOrParamTypeEnum(FieldOrParamTypeEnum.Collection);

                List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(genericClazz);
                List<Object> collect = beanDefinitions.stream().map(beanDefinition1 -> {
                    BeanDefinition bean1 = beanCreateContext.createBean(beanDefinition1);
                    return bean1.getBean();
                }).collect(Collectors.toList());
                ReflectUtil.setFieldValue(bean, fieldDefinition.getField(), collect);
            } else {
                //集合类型，没有泛型
                throw new RuntimeException(fieldDefinition.toString() + ";如果注入集合类型，需要明确泛型。");
            }
        } else {
            fieldDefinition.setFieldOrParamTypeEnum(FieldOrParamTypeEnum.Simple);
            BeanDefinition injectBeanDefinition = beanDefinitionContext.getInjectBeanDefinition(beanName, fieldDefinition.getFieldType());
            Object bean1 = beanCreateContext.createBean(injectBeanDefinition).getBean();
            ReflectUtil.setFieldValue(bean, fieldDefinition.getField(), bean1);
        }
    }

    private Object getBean(BeanDefinition beanDefinition) {
        Object bean;
        if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.factoryBean) {
            bean = beanDefinition.getFactoryBean();
        } else {
            bean = beanDefinition.getBean();
        }
        return bean;
    }
}
