package xyz.xiezc.ioc.annotation.handler;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.FieldOrParamTypeEnum;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;


@Component
public class InjectAnnotationHandler extends AnnotationHandler<Inject> {

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public Class<Inject> getAnnotationType() {
        return Inject.class;
    }

    @Override
    public void processClass(Inject annotation, Class clazz) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Inject annotation, BeanDefinition beanDefinition) {

    }

    /**
     * 这个方法就是设置fieldDefinition中的beanName字段值。
     *
     * @param fieldDefinition 被注解的字段
     * @param annotation      这个类上的所有注解
     * @param beanDefinition  被注解的类
     */
    @Override
    public void processField(FieldDefinition fieldDefinition, Inject annotation, BeanDefinition beanDefinition) {
        String beanName = annotation.value();
        if (StrUtil.isBlank(beanName)) {
            beanName = fieldDefinition.getFieldName();
        }

        Class<?> fieldType = fieldDefinition.getFieldType();
        //如果是数组类型，则获取数组中的真实类型
        if (fieldType.isArray()) {
            Class<?> componentType = fieldType.getComponentType();
            fieldDefinition.setFieldType(componentType);
            fieldDefinition.setFieldOrParamTypeEnum(FieldOrParamTypeEnum.Array);
        }
        //如果是集合类型， 就获取泛型值
        if (ClassUtil.isAssignable(Collection.class, fieldType)) {
            Field declaredField = ClassUtil.getDeclaredField(beanDefinition.getBeanClass(), fieldDefinition.getFieldName());
            Type genericType = declaredField.getGenericType();
            if (genericType != null && genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                //得到泛型里的class类型对象
                Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];
                fieldDefinition.setFieldType(genericClazz);
                fieldDefinition.setFieldOrParamTypeEnum(FieldOrParamTypeEnum.Collection);
            }
        }
    }
}
