package xyz.xiezc.ioc.system.common.context;

import xyz.xiezc.ioc.system.event.ApplicationEvent;
import xyz.xiezc.ioc.system.event.ApplicationListener;

/**
 * 事件监听器接口 管理类
 */
public interface EventPublisherContext {


    /**
     * Add a listener to be notified of all events.
     *
     * @param listener the listener to add
     */
    void addApplicationListener(String eventName, ApplicationListener listener);

    /**
     * Remove a listener from the notification list.
     *
     * @param eventName the listener to remove
     */
    void removeApplicationListener(String eventName);

    /**
     * Remove all listeners registered with this multicaster.
     * <p>After a remove call, the multicaster will perform no action
     * on event notification until new listeners are registered.
     */
    void removeAllListeners();

    /**
     * Multicast the given application event to appropriate listeners.
     * if possible as it provides better support for generics-based events.
     *
     * @param event the event to multicast
     */
     void publisherEvent(ApplicationEvent event);
}
