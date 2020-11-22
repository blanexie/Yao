package xyz.xiezc.ioc.starter.eventListener;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 *
 */
public class DefaultEventDispatcher implements EventDispatcher {

    ExecutorService executorService;


    Map<String, List<ApplicationListener>> listenerMap = new HashMap<>();


    @Override
    public void setThreadPool(ExecutorService executorService) {
        this.executorService = executorService;
    }


    @Override
    public void addListener(ApplicationListener applicationListener) {
        Set<String> eventNames = applicationListener.dealEventName();
        for (String eventName : eventNames) {
            List<ApplicationListener> applicationListeners = listenerMap.get(eventName);
            if (applicationListeners == null) {
                applicationListeners = new ArrayList<>();
                listenerMap.put(eventName, applicationListeners);
            }
            applicationListeners.add(applicationListener);
        }
    }

    @Override
    public List<ApplicationListener> getListener(String eventName) {
        return listenerMap.getOrDefault(eventName, new ArrayList<>());
    }

    @Override
    public void dispatcher(ApplicationEvent applicationEvent) {
        List<ApplicationListener> listener = this.getListener(applicationEvent.getEventName());
        for (ApplicationListener applicationListener : listener) {
            if (applicationListener.getSync()) {
                applicationListener.doExecute(applicationEvent);
            } else {
                executorService.execute(() -> {
                    applicationListener.doExecute(applicationEvent);
                });
            }
        }
    }

}
