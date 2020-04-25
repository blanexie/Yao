package xyz.xiezc.ioc.common.context.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import xyz.xiezc.ioc.common.context.EventPublisherContext;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventPublisherContextUtil implements EventPublisherContext {

    Map<String, List<ApplicationListener>> listenerMap = new HashMap();

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        List<ApplicationListener> applicationListeners = listenerMap.get(listener.getEventName());
        applicationListeners = CollUtil.defaultIfEmpty(applicationListeners, new ArrayList<>());
        applicationListeners.add(listener);
        listenerMap.put(listener.getEventName(), applicationListeners);
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        List<ApplicationListener> applicationListeners = listenerMap.get(listener.getEventName());
        applicationListeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        listenerMap.clear();
    }

    @Override
    public void multicastEvent(ApplicationEvent event) {
        List<ApplicationListener> applicationListeners = listenerMap.get(event.getEventName());
        applicationListeners.forEach(applicationListener -> {
            applicationListener.execute(event);
        });
    }
}
