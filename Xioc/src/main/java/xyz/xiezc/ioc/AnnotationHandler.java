package xyz.xiezc.ioc;

import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.AnnotationOrder;
import xyz.xiezc.ioc.definition.BeanSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 注解处理器， 每一个注解都应该对应一个处理器。
 *
 * @author wb-xzc291800
 * @date 2019/04/04 15:01
 */
public abstract class AnnotationHandler<T extends Annotation> extends AnnotationOrder {


    public abstract Class<T> getAnnotationType();


    /**
     * 如果是在类注解， 则会调用这个方法处理
     *
     * @param clazz       被注解类的基本信息
     * @param annotation  这个类上的所有注解
     * @param contextUtil 容器
     */
    public abstract void processClass(T annotation, Class clazz, ContextUtil contextUtil);

    /**
     * 如果是方法注解，则会调用这个方法来处理
     *
     * @param beanSignature 被注解的类
     * @param method        被注解的方法
     * @param annotation    这个类上的所有注解
     * @param contextUtil   容器中已有的所有bean信息
     */
    public abstract void processMethod(Method method, T annotation, BeanSignature beanSignature, ContextUtil contextUtil);

    /**
     * 如果是字段注解，则会调用这个方法来处理
     *
     * @param beanSignature 被注解的类
     * @param field         被注解的字段
     * @param annotation    这个类上的所有注解
     * @param contextUtil   容器中已有的所有bean信息
     */
    public abstract void processField(Field field, T annotation, BeanSignature beanSignature, ContextUtil contextUtil);

}
