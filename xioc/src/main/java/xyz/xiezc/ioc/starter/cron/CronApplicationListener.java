package xyz.xiezc.ioc.starter.cron;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.log.Log;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.common.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.eventListener.ApplicationEvent;
import xyz.xiezc.ioc.starter.eventListener.ApplicationListener;

import java.util.*;

@Component
public class CronApplicationListener implements ApplicationListener {

    Log log= Log.get(CronApplicationListener.class);

    Set<String> eventNames = new HashSet<>() {{
        add(EventNameConstant.finishEvent);
    }};


    Map<String, List<Task>> cronMap = new HashMap<>();

    public void addCronTask(String cron, Task task) {
        List<Task> tasks = cronMap.get(cron);
        if (tasks == null) {
            tasks = new ArrayList<>();
            cronMap.put(cron, tasks);
        }
        tasks.add(task);
    }


    @Override
    public Set<String> dealEventName() {
        return eventNames;
    }

    @Override
    public void doExecute(ApplicationEvent applicationEvent) {
        cronMap.forEach((k,v)->{
            for (Task task : v) {
                CronUtil.schedule(k,task);
            }
        });
        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start(true);
        log.info("定时任务启动完成......");
    }

    @Override
    public boolean getSync() {
        return false;
    }
}
