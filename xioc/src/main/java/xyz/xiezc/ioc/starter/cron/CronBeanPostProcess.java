package xyz.xiezc.ioc.starter.cron;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.cron.Cron;
import xyz.xiezc.ioc.starter.core.context.BeanFactory;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanPostProcess;

import java.lang.reflect.Method;

@Component
public class CronBeanPostProcess implements BeanPostProcess {

    @Override
    public int order() {
        return 90;
    }

    @Autowire
    CronApplicationListener cronApplicationListener;

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
        //查找切面方法
        Method[] publicMethods = ClassUtil.getPublicMethods(beanClass);
        for (Method declaredMethod : publicMethods) {
            Cron cron = AnnotationUtil.getAnnotation(declaredMethod, Cron.class);
            if (cron == null) {
                continue;
            }
            //过滤有参数的方法
            int parameterCount = declaredMethod.getParameterCount();
            if (parameterCount > 0) {
                throw new RuntimeException("cron 注解的方法不能有参数, method:" + declaredMethod);
            }
            String cronStr = cron.value();
            cronApplicationListener.addCronTask(cronStr, () -> ReflectUtil.invoke(beanDefinition.getCompletedBean(), declaredMethod));
        }
        return true;
    }
}
