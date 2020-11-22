package xyz.xiezc.ioc.starter.aop;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.starter.annotation.aop.Aop;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.core.context.BeanFactory;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanPostProcess;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Component
public class AopBeanPostProcess implements BeanPostProcess {

    @Override
    public int order() {
        return -100;
    }

    private Set<Method> notAopMethods = new HashSet<>() {{
        Object o = new Object();
        //o.wait();
        Method wait = ClassUtil.getPublicMethod(Object.class, "wait");
        add(wait);
        //o.wait(1);
        Method wait1 = ClassUtil.getPublicMethod(Object.class, "wait", long.class);
        add(wait1);
        //o.wait(0,1);
        Method wait2 = ClassUtil.getPublicMethod(Object.class, "wait", long.class, int.class);
        add(wait2);
        //  o.getClass();
        Method wait3 = ClassUtil.getPublicMethod(Object.class, "getClass");
        add(wait3);
        //o.toString();
        Method wait4 = ClassUtil.getPublicMethod(Object.class, "toString");
        add(wait4);
        Method wait5 = ClassUtil.getPublicMethod(Object.class, "equals", Object.class);
        add(wait5);
        Method wait6 = ClassUtil.getPublicMethod(Object.class, "notifyAll");
        add(wait6);
        Method wait7 = ClassUtil.getPublicMethod(Object.class, "hashCode");
        add(wait7);
        Method wait8 = ClassUtil.getPublicMethod(Object.class, "notify");
        add(wait8);
    }};


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
        Set<XAspect> xAspectSet = new HashSet<>();
        Method[] publicMethods = ClassUtil.getPublicMethods(beanClass);
        for (Method declaredMethod : publicMethods) {
            if (notAopMethods.contains(declaredMethod)) {
                continue;
            }
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
