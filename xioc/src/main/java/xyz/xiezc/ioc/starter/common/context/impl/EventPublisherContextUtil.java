package xyz.xiezc.ioc.starter.common.context.impl;

import cn.hutool.core.collection.CollUtil;
import xyz.xiezc.ioc.starter.common.context.EventPublisherContext;
import xyz.xiezc.ioc.starter.event.ApplicationEvent;
import xyz.xiezc.ioc.starter.event.ApplicationListener;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 事件监听处理的相关逻辑，
 */
public class EventPublisherContextUtil implements EventPublisherContext {

    Map<String, List<ApplicationListener>> listenersMap = new HashMap<>();


    @Override
    public List<ApplicationListener> getApplicationListener(String eventName) {
        List<ApplicationListener> applicationListeners = listenersMap.get(eventName);
        return applicationListeners;
    }

    @Override
    public void addApplicationListener(String eventName, ApplicationListener listener) {
        List<ApplicationListener> applicationListeners = listenersMap.get(eventName);
        if (CollUtil.isEmpty(applicationListeners)) {
            applicationListeners = CollUtil.newArrayList();
        }
        if (applicationListeners.contains(listener)) {
            return;
        }

        applicationListeners.add(listener);
        applicationListeners = applicationListeners.stream()
                .sorted((a, b) -> {
                    int orderA = a.order();
                    int orderB = b.order();
                    return orderA - orderB > 0 ? 1 : -1;
                }).collect(Collectors.toList());
        listenersMap.put(eventName, applicationListeners);
    }

    @Override
    public void removeApplicationListener(String eventName) {
        List<ApplicationListener> applicationListeners = listenersMap.get(eventName);
        applicationListeners.remove(eventName);
        listenersMap.put(eventName, applicationListeners);
    }

    @Override
    public void removeAllListeners() {
        listenersMap.clear();
    }

    @Override
    public void publisherEvent(ApplicationEvent event) {
        List<ApplicationListener> applicationListeners = listenersMap.get(event.getEventName());
        if (CollUtil.isEmpty(applicationListeners)) {
            return;
        }
        CopyOnWriteArrayList<ApplicationListener> applicationListeners1 = new CopyOnWriteArrayList<>(applicationListeners);
        for (ApplicationListener applicationListener : applicationListeners1) {
            applicationListener.execute(event);
        }
    }
}
