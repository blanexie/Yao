package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.core.Init;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.core.definition.ParamDefinition;

import java.lang.annotation.Annotation;

@Data
public class InitAnnotationHandler extends AnnotationHandler<Init> {




    @Override
    public Class<Init> getAnnotationType() {
        return Init.class;
    }

    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {

    }


    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation init, BeanDefinition beanDefinition) {
        ParamDefinition[] paramDefinitions = methodDefinition.getParamDefinitions();
        if (paramDefinitions != null && paramDefinitions.length != 0) {
            ExceptionUtil.wrapAndThrow(new RuntimeException("bean的init方法只能无参，" + methodDefinition));
        }
        beanDefinition.setInitMethodDefinition(methodDefinition);

        //调用对应bean的init方法
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        if (initMethodDefinition != null) {
            Object bean = beanDefinition.getBean();
            ReflectUtil.invoke(bean, initMethodDefinition.getMethod());
        }
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }
}
