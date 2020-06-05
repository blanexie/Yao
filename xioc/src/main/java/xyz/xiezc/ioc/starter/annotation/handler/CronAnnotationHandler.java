package xyz.xiezc.ioc.starter.annotation.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.starter.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;
import xyz.xiezc.ioc.starter.annotation.Cron;
import xyz.xiezc.ioc.starter.annotation.EnableCron;
import xyz.xiezc.ioc.starter.common.cron.CronApplicationListener;
import xyz.xiezc.ioc.starter.common.cron.CronDefinition;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.definition.FieldDefinition;
import xyz.xiezc.ioc.starter.common.definition.MethodDefinition;

import java.lang.annotation.Annotation;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/5 11:54 上午
 **/
@Data
public class CronAnnotationHandler extends AnnotationHandler<Cron> {

    Log log = LogFactory.get(CronAnnotationHandler.class);

    private ApplicationContextUtil applicationContextUtil;

    @Override
    public Class<Cron> getAnnotationType() {
        return Cron.class;
    }

    @Override
    public void processClass(Annotation annotation, Class clazz, BeanDefinition beanDefinition) {

    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, Annotation annotation, BeanDefinition beanDefinition) {
        //查看是否启用了cron
        Class bootClass = Xioc.bootClass;
        EnableCron enableCron = AnnotationUtil.getAnnotation(bootClass, EnableCron.class);
        if (enableCron == null) {
            throw new RuntimeException("启动类上没有EnableCron注解， 无法启动定时任务");
        }
        //校验
        Cron cron = (Cron) annotation;
        String value = cron.value();
        if (StrUtil.isBlank(value)) {
            throw new RuntimeException("请配置定时任务cron表达式");
        }

        //放入监听器中
        BeanDefinition beanDefinition1 = applicationContextUtil.getBeanDefinitionContext().getBeanDefinition(CronApplicationListener.class);
        applicationContextUtil.getBeanCreateContext().createBean(beanDefinition1);
        CronApplicationListener cronApplicationListener = beanDefinition1.getBean();
        CronDefinition cronDefinition = new CronDefinition();
        cronDefinition.setBeanDefinition(beanDefinition);
        cronDefinition.setCron(cron);
        cronDefinition.setMethodDefinition(methodDefinition);
        cronApplicationListener.getCronDefinitionList().add(cronDefinition);
    }

    @Override
    public void processField(FieldDefinition fieldDefinition, Annotation annotation, BeanDefinition beanDefinition) {

    }
}