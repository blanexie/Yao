package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.annotation.Aop;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.starter.annotation.Inject;
import xyz.xiezc.ioc.system.common.AopAspect;
import xyz.xiezc.ioc.system.common.context.BeanCreateContext;
import xyz.xiezc.ioc.system.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.definition.FieldDefinition;
import xyz.xiezc.ioc.system.common.definition.MethodDefinition;

import java.lang.reflect.Method;

@Component
public class AopAnnotationHandler extends AnnotationHandler<Aop> {

    @Override
    public Class<Aop> getAnnotationType() {
        return Aop.class;
    }

    @Inject
    BeanCreateContext beanCreateContext;

    @Inject
    BeanDefinitionContext beanDefinitionContext;

    @Override
    public void processClass(Aop annotation, Class clazz) {
        //获取切面类
        BeanDefinition aopAspectBeanDefinition = beanDefinitionContext.getBeanDefinition(annotation.value());
        beanCreateContext.newInstance(aopAspectBeanDefinition);
        AopAspect aspectBean = aopAspectBeanDefinition.getBean();
        //获取所有被切的方法
        Method[] publicMethods = ClassUtil.getPublicMethods(clazz);
        for (Method publicMethod : publicMethods) {
            aspectBean.addMethod(publicMethod);
        }
        //获取bean实例
        BeanDefinition beanDefinition = beanDefinitionContext.getBeanDefinition(clazz);
        beanCreateContext.newInstance(aopAspectBeanDefinition);
        Object parentBean = beanDefinition.getBean();
        beanDefinition.setBean(ProxyUtil.proxy(parentBean, aspectBean));
    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Aop annotation, BeanDefinition beanDefinition) {
        //获取切面类
        BeanDefinition aopAspectBeanDefinition = beanDefinitionContext.getBeanDefinition(annotation.value());
        beanCreateContext.newInstance(aopAspectBeanDefinition);
        AopAspect aspectBean = aopAspectBeanDefinition.getBean();
        //获取被切方法
        Method method = methodDefinition.getMethod();
        if (ClassUtil.isPublic(method)) {
            aspectBean.addMethod(method);
        }
        //设置bean实例
        BeanDefinition parentBeanDefinition = methodDefinition.getBeanDefinition();
        beanCreateContext.newInstance(aopAspectBeanDefinition);
        Object parentBean = parentBeanDefinition.getBean();
        beanDefinition.setBean(ProxyUtil.proxy(parentBean, aspectBean));
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Aop annotation, BeanDefinition beanDefinition) {

    }
}
