package xyz.xiezc.ioc.starter.aop;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.aop.aspects.Aspect;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.starter.annotation.aop.Aop;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.core.context.BeanFactory;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanPostProcess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Component
public class AopBeanPostProcess implements BeanPostProcess {

    @Override
    public boolean beforeInstance(BeanFactory beanFactory, BeanDefinition beanDefinition) {
        return false;
    }

    @Override
    public boolean beforeInject(BeanFactory beanFactory, BeanDefinition beanDefinition) {
        return false;
    }

    @Override
    public boolean beforeInit(BeanFactory beanFactory, BeanDefinition beanDefinition) {
        return false;
    }

    @Override
    public boolean afterInit(BeanFactory beanFactory, BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Aop aop = AnnotationUtil.getAnnotation(beanClass, Aop.class);
        //查找切面方法
        Set<XAspect> xAspectSet=new HashSet<>();
        Method[] declaredMethods = ClassUtil.getPublicMethods(beanClass);
        for (Method declaredMethod : declaredMethods) {
            Aop aopMethod = AnnotationUtil.getAnnotation(declaredMethod, Aop.class);
            Class<? extends XAspect> value = null;
            if (aop != null) {
                value = aop.value();
            }
            if (aopMethod != null) {
                value = aopMethod.value();
            }
            if (value != null) {
                BeanDefinition aopBeanDefinition = beanFactory.getBeanDefinition(value);
                XAspect xAspect = aopBeanDefinition.getBean();
                xAspect.addMethod(declaredMethod);
                xAspectSet.add(xAspect);
            }
        }

        for (XAspect xAspect : xAspectSet) {
            Object proxy = ProxyUtil.proxy(beanDefinition.getBean(), xAspect);
            beanDefinition.setBean(proxy);
        }

        return true;
    }
}
