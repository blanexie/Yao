package xyz.xiezc.ioc.starter.eventListener;

import java.util.Set;

/**
 * 事件监听器
 */
public interface ApplicationListener {

    /**
     * 这个监听器需要处理的事件
     *
     * @return
     */
    Set<String> dealEventName();

    /**
     * 时间的处理逻辑
     *
     * @param applicationEvent
     */
    void doExecute(ApplicationEvent applicationEvent);

    /**
     * 是否同步调用事件， 默认是的
     *
     * @return
     */
    default boolean getSync() {
        return true;
    }

}