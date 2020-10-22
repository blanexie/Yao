package xyz.xiezc.ioc.starter.annotation.handler;

import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.annotation.core.Bean;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;

import java.lang.annotation.Annotation;

/**
 * @Description bean注解处理器
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/11 6:00 下午
 **/
@Data
public class BeanAnnotationHandler extends AnnotationHandler<Bean> {


    @Override
    public Class<Bean> getAnnotationType() {
        return Bean.class;
    }

    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {
        beanDefinition.getMethodDefinitions().add(methodDefinition);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }
}