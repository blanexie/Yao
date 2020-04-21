package xyz.xiezc.ioc.starter.annotationHandler;

import cn.hutool.core.exceptions.ExceptionUtil;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Init;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;

public class InitAnnotationHandler extends AnnotationHandler<Init> {

    @Override
    public Class<Init> getAnnotationType() {
        return Init.class;
    }

    @Override
    public void processClass(Init annotation, Class clazz, ContextUtil contextUtil) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Init annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        if (paramDefinitions != null && paramDefinitions.length != 0) {
            ExceptionUtil.wrapAndThrow(new RuntimeException("bean的init方法只能无参，"+methodDefinition));
        }
        beanDefinition.setInitMethodDefinition(methodDefinition);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Init annotation, BeanDefinition beanDefinition, ContextUtil contextUtil) {

    }
}
