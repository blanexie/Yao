package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.starter.annotation.Inject;
import xyz.xiezc.ioc.starter.annotation.Value;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.system.common.NullObj;
import xyz.xiezc.ioc.system.common.context.impl.PropertiesContextUtil;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.enums.FieldOrParamTypeEnum;

@Component
public class ValueAnnotationHandler extends AnnotationHandler<Value> {
    @Override
    public Class<Value> getAnnotationType() {
        return Value.class;
    }

    @Inject
    ApplicationContextUtil contextUtil;

    @Inject
    PropertiesContextUtil propertiesContextUtil;

    @Override
    public void processClass(Value annotation, Class clazz) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Value annotation, BeanDefinition beanDefinition) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Value annotation, BeanDefinition beanDefinition) {
        String value = annotation.value();
        if (StrUtil.isBlank(value)) {
            value = fieldDefinition.getFieldName();
        }
        fieldDefinition.setFieldOrParamTypeEnum(FieldOrParamTypeEnum.Properties);
        String str = propertiesContextUtil.getSetting().getStr(value);
        if (StrUtil.isBlank(str)) {
            fieldDefinition.setFieldValue(NullObj.NULL);
        } else {
            fieldDefinition.setFieldValue(str);
        }
    }
}
