package xyz.xiezc.ioc.common.event;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件监听模式的包
 */
public class EventListenerUtil {

    Map<Event, List<Listener>> listenerMap = new HashMap<>();


    public void addListener(Event event, Listener listener) {
        List<Listener> listeners = listenerMap.get(event);
        if (listeners == null) {
            listeners = new ArrayList<>();
            listenerMap.put(event, listeners);
        }
        listeners.add(listener);
    }


    public void removeListener(Event event, Listener listener) {
        List<Listener> listeners = listenerMap.get(event);
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
    }

    public List<Listener> removeEvent (Event event) {

        return listenerMap.remove(event);
    }

}
