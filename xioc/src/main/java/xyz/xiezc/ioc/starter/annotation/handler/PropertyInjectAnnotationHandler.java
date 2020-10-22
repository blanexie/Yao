package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.util.ReflectUtil;
import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.annotation.*;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.common.context.PropertiesContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/29 11:37 上午
 **/
public class PropertyInjectAnnotationHandler extends AnnotationHandler<PropertyInject> {
    @Override
    public Class<PropertyInject> getAnnotationType() {
        return PropertyInject.class;
    }


    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {
        Configuration configuration = AnnotationUtil.getAnnotation(clazz, Configuration.class);
        if (configuration == null) {
            throw new RuntimeException(clazz.getName() + "其上的注解PropertyInject必须和Configuration配合一起使用");
        }
        PropertyInject propertyInject = (PropertyInject) annotation;
        String prefix = propertyInject.prefix();
        PropertiesContext propertiesContext = Xioc.getApplicationContext().getPropertiesContext();

        Class<?> beanClass = beanDefinition.getBeanClass();
        Field[] fields = ReflectUtil.getFields(beanClass);

        for (Map.Entry<String, String> entry : propertiesContext.getSetting().entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (k.startsWith(prefix)) {
                for (Field field : fields) {
                    String name = field.getName();
                    String replace = k.replace(prefix, "");
                    if (replace.startsWith(".")) {
                        replace = replace.substring(1);
                    }
                    if (Objects.equals(name, replace)) {
                        Object covert = this.covert(field.getType(), v);
                        if (covert == null) {
                            continue;
                        }
                        ReflectUtil.setFieldValue(beanDefinition.getBean(), field, covert);
                    }
                }
            }
        }
    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }

    public <T> T covert(Class<T> clazz, String value) {
        try {
            return Convert.convert(clazz, value);
        } catch (ConvertException e) {
            return null;
        }
    }
}