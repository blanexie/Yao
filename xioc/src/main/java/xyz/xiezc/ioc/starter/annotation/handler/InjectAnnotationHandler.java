package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import xyz.xiezc.ioc.starter.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.common.context.BeanCreateContext;
import xyz.xiezc.ioc.starter.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Data
public class InjectAnnotationHandler extends AnnotationHandler<Autowire> {


    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public Class<Autowire> getAnnotationType() {
        return Autowire.class;
    }

    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }

    /**
     * 这个方法就是注入依赖的字段。 在注入依赖前需要先判断这个被注入的依赖是否是切面类， 如果是切面类。
     *
     * @param fieldDefinition 被注解的字段
     * @param annotation      这个类上的所有注解
     * @param beanDefinition  被注解的类
     */
    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {
        ApplicationContextUtil applicationContextUtil = Xioc.getApplicationContext();


        BeanDefinitionContext beanDefinitionContext = applicationContextUtil.getBeanDefinitionContext();
        BeanCreateContext beanCreateContext = applicationContextUtil.getBeanCreateContext();
        String beanName = ((Autowire) annotation).value();
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
                if (beanDefinition1.getBean() == null || beanDefinition1.getBeanStatus() == BeanStatusEnum.Original) {
                    beanCreateContext.createBean(beanDefinition1);
                }
                return beanDefinition1.getBean();
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
                    if (beanDefinition1.getBean() == null || beanDefinition1.getBeanStatus() == BeanStatusEnum.Original) {
                        beanCreateContext.createBean(beanDefinition1);
                    }
                    return beanDefinition1.getBean();
                }).collect(Collectors.toList());
                ReflectUtil.setFieldValue(bean, fieldDefinition.getField(), collect);
            } else {
                //集合类型，没有泛型
                throw new RuntimeException(fieldDefinition.toString() + ";如果注入集合类型，需要明确泛型。");
            }
        } else {
            fieldDefinition.setFieldOrParamTypeEnum(FieldOrParamTypeEnum.Simple);
            BeanDefinition injectBeanDefinition = beanDefinitionContext.getInjectBeanDefinition(beanName, fieldDefinition.getFieldType());
            if (injectBeanDefinition.getBean() == null || injectBeanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
                beanCreateContext.createBean(injectBeanDefinition);
            }
            ReflectUtil.setFieldValue(bean, fieldDefinition.getField(), injectBeanDefinition.getBean());
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
