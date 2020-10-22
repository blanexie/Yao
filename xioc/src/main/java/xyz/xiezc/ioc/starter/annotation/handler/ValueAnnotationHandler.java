package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import xyz.xiezc.ioc.starter.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.annotation.core.Value;
import xyz.xiezc.ioc.starter.common.NullObj;
import xyz.xiezc.ioc.starter.common.context.PropertiesContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.common.enums.FieldOrParamTypeEnum;

import java.lang.annotation.Annotation;

@Data
public class ValueAnnotationHandler extends AnnotationHandler<Value> {

    @Override
    public Class<Value> getAnnotationType() {
        return Value.class;
    }


    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {
        ApplicationContextUtil applicationContextUtil = Xioc.getApplicationContext();
        PropertiesContext propertiesContext = applicationContextUtil.getPropertiesContext();

        Object bean = beanDefinition.getBean();
        String value = ((Value) annotation).value();
        if (StrUtil.isBlank(value)) {
            value = fieldDefinition.getFieldName();
        }
        fieldDefinition.setFieldOrParamTypeEnum(FieldOrParamTypeEnum.Properties);
        String str = propertiesContext.getSetting().getStr(value);
        if (StrUtil.isBlank(str)) {
            fieldDefinition.setFieldValue(NullObj.NULL);
        } else {
            fieldDefinition.setFieldValue(str);
            Class<?> fieldType = fieldDefinition.getFieldType();
            Object convert = Convert.convert(fieldType, str);
            ReflectUtil.setFieldValue(bean, fieldDefinition.getField(), convert);
        }
    }
}
