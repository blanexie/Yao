package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.starter.annotation.Init;
import xyz.xiezc.ioc.system.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.annotation.Inject;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;
import xyz.xiezc.ioc.system.common.definition.ParamDefinition;

@Component
public class InitAnnotationHandler extends AnnotationHandler<Init> {

    @Override
    public Class<Init> getAnnotationType() {
        return Init.class;
    }

    @Override
    public void processClass(Init annotation, Class clazz) {

    }

    @Inject
    ApplicationContextUtil contextUtil;

    @Override
    public void processMethod(MethodDefinition methodDefinition, Init init, BeanDefinition beanDefinition) {
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        if (paramDefinitions != null && paramDefinitions.length != 0) {
            ExceptionUtil.wrapAndThrow(new RuntimeException("bean的init方法只能无参，" + methodDefinition));
        }
        beanDefinition.setInitMethodDefinition(methodDefinition);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Init annotation, BeanDefinition beanDefinition) {

    }
}
