package xyz.xiezc.ioc.starter.annotationHandler;

import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;

public class ValueAnnotationHandler extends AnnotationHandler<Value> {
    @Override
    public Class<Value> getAnnotationType() {
        return Value.class;
    }

    @Override
    public void processClass(Value annotation, Class clazz, ContextUtil contextUtil) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Value annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Value annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {
        String beanName = annotation.value();
        if (StrUtil.isBlank(beanName)) {
            beanName = fieldDefinition.getFieldName();
        }
        fieldDefinition.setBeanName(beanName);

        String str = contextUtil.getSetting().getStr(beanName);
        fieldDefinition.setObj(str);
    }
}
