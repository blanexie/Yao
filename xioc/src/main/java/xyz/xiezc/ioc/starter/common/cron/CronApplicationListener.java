package xyz.xiezc.ioc.starter.common.cron;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.cron.Cron;
import xyz.xiezc.ioc.starter.annotation.listener.EventListener;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.common.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.event.ApplicationEvent;
import xyz.xiezc.ioc.starter.event.ApplicationListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/5 11:57 上午
 **/
@Data
@EventListener(eventName = EventNameConstant.XiocEnd)
public class CronApplicationListener implements ApplicationListener {

    Log log = LogFactory.get(CronApplicationListener.class);

    List<CronDefinition> cronDefinitionList = new ArrayList<>();


    @Override
    public int order() {
        return 0;
    }

    @Override
    public void doExecute(ApplicationEvent applicationEvent) {
        cronDefinitionList.forEach(cronDefinition -> {
            Cron cron = cronDefinition.getCron();
            MethodDefinition methodDefinition = cronDefinition.getMethodDefinition();
            BeanDefinition beanDefinition = methodDefinition.getBeanDefinition();
            Object bean = beanDefinition.getBean();
            Method method = methodDefinition.getMethod();
            CronUtil.schedule(cron.value(), (Task) () -> ReflectUtil.invoke(bean, method));
            // 支持秒级别定时任务
            CronUtil.setMatchSecond(true);
            log.info("定时任务已经就绪，bean:{},  method:{} , cron:{}", beanDefinition.getBeanClass().getName(), method.getName(), cron.value());
        });
        CronUtil.start(true);
        log.info("开始启动所有的定时任务.......................");
    }
}