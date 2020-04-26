package xyz.xiezc.ioc.annotation.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Init;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.definition.ParamDefinition;
@Component
public class InitAnnotationHandler extends AnnotationHandler<Init> {

    @Override
    public Class<Init> getAnnotationType() {
        return Init.class;
    }

    @Override
    public void processClass(Init annotation, Class clazz, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Init annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        if (paramDefinitions != null && paramDefinitions.length != 0) {
            ExceptionUtil.wrapAndThrow(new RuntimeException("bean的init方法只能无参，"+methodDefinition));
        }
        beanDefinition.setInitMethodDefinition(methodDefinition);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Init annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
