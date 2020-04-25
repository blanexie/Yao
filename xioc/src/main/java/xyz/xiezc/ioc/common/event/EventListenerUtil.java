package xyz.xiezc.ioc.common.event;


import cn.hutool.core.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件监听模式的包
 */
public class EventListenerUtil {

    Map<ApplicationEvent, List<ApplicationListener>> listenerMap = new ConcurrentHashMap<>();

    public void asyncCall(ApplicationEvent applicationEvent) {
        ThreadUtil.execute(() -> {
            this.syncCall(applicationEvent);
        });
    }

    public void syncCall(ApplicationEvent applicationEvent) {
        List<ApplicationListener> applicationListeners = listenerMap.get(applicationEvent);
        if (applicationListeners == null) {
            return;
        }
        CopyOnWriteArrayList<ApplicationListener> copyOnWriteArrayList = new CopyOnWriteArrayList<>(applicationListeners);
        for (ApplicationListener applicationListener : copyOnWriteArrayList) {
            applicationListener.execute(applicationEvent);
        }
    }

    public void addListener(ApplicationEvent applicationEvent, ApplicationListener applicationListener) {
        List<ApplicationListener> applicationListeners = listenerMap.get(applicationEvent);
        if (applicationListeners == null) {
            applicationListeners = new ArrayList<>();
            listenerMap.put(applicationEvent, applicationListeners);
        }
        applicationListeners.add(applicationListener);
        applicationListeners.sort((a, b) -> {
            int orderA = a.order();
            int orderB = b.order();
            return orderA > orderB ? 1 : -1;
        });
    }


    public void removeListener(ApplicationEvent applicationEvent, ApplicationListener applicationListener) {
        List<ApplicationListener> applicationListeners = listenerMap.get(applicationEvent);
        if (applicationListeners == null) {
            return;
        }
        applicationListeners.remove(applicationListener);
    }

    public List<ApplicationListener> removeEvent(ApplicationEvent applicationEvent) {
        return listenerMap.remove(applicationEvent);
    }

}
