package xyz.xiezc.ioc.annotation.handler;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Aop;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.common.AopAspect;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;

import java.lang.reflect.Method;

@Component
public class AopAnnotationHandler extends AnnotationHandler<Aop> {

    @Override
    public Class<Aop> getAnnotationType() {
        return Aop.class;
    }

    @Override
    public void processClass(Aop annotation, Class clazz, ApplicationContextUtil contextUtil) {
        //获取切面类
        BeanDefinition AopAspectBeanDefinition = contextUtil.getBeanDefinition(annotation.value());
        contextUtil.newInstance(AopAspectBeanDefinition);
        AopAspect aspectBean = AopAspectBeanDefinition.getBean();
        //获取所有被切的方法
        Method[] publicMethods = ClassUtil.getPublicMethods(clazz);
        for (Method publicMethod : publicMethods) {
            aspectBean.addMethod(publicMethod);
        }
        //获取bean实例
        BeanDefinition beanDefinition = contextUtil.getBeanDefinition(clazz);
        contextUtil.newInstance(beanDefinition);
        Object parentBean = beanDefinition.getBean();
        beanDefinition.setBean(ProxyUtil.proxy(parentBean, aspectBean));
    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Aop annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {
        //获取切面类
        BeanDefinition AopAspectBeanDefinition = contextUtil.getBeanDefinition(annotation.value());
        contextUtil.newInstance(AopAspectBeanDefinition);
        AopAspect aspectBean = AopAspectBeanDefinition.getBean();
        //获取被切方法
        Method method = methodDefinition.getMethod();
        if (ClassUtil.isPublic(method)) {
            aspectBean.addMethod(method);
        }
        //设置bean实例
        BeanDefinition parentBeanDefinition = methodDefinition.getBeanDefinition();
        contextUtil.newInstance(parentBeanDefinition);
        Object parentBean = parentBeanDefinition.getBean();
        beanDefinition.setBean(ProxyUtil.proxy(parentBean, aspectBean));
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Aop annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
