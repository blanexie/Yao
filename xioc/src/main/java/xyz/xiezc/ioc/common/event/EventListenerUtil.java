package xyz.xiezc.ioc.common.event;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件监听模式的包
 */
public class EventListenerUtil {

    Map<Event, List<Listener>> listenerMap = new ConcurrentHashMap<>();

    public void asyncCall(Event event) {
        ThreadUtil.execute(() -> {
            this.syncCall(event);
        });
    }

    public void syncCall(Event event) {
        List<Listener> listeners = listenerMap.get(event);
        if (listeners == null) {
            return;
        }
        CopyOnWriteArrayList<Listener> copyOnWriteArrayList = new CopyOnWriteArrayList<>(listeners);
        for (Listener listener : copyOnWriteArrayList) {
            listener.execute(event);
        }
    }

    public void addListener(Event event, Listener listener) {
        List<Listener> listeners = listenerMap.get(event);
        if (listeners == null) {
            listeners = new ArrayList<>();
            listenerMap.put(event, listeners);
        }
        listeners.add(listener);
        listeners.sort((a, b) -> {
            int orderA = a.order();
            int orderB = b.order();
            return orderA > orderB ? 1 : -1;
        });
    }


    public void removeListener(Event event, Listener listener) {
        List<Listener> listeners = listenerMap.get(event);
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
    }

    public List<Listener> removeEvent(Event event) {
        return listenerMap.remove(event);
    }

}
