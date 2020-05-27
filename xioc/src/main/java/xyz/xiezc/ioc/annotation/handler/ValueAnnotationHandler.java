package xyz.xiezc.ioc.annotation.handler;

import cn.hutool.Hutool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONNull;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.common.NullObj;
import xyz.xiezc.ioc.common.context.PropertiesContext;
import xyz.xiezc.ioc.common.context.impl.PropertiesContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.FieldOrParamTypeEnum;

import javax.lang.model.type.NullType;

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
            fieldDefinition.setObj(NullObj.NULL);
        } else {
            fieldDefinition.setObj(str);
        }
    }
}
