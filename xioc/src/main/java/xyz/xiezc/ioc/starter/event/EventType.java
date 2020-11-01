package xyz.xiezc.ioc.starter.event;

/**
 * 事件的类型
 */
public enum EventType {

    /**
     * 广播事件。 事件会被所有的该事件监听器收到并执行。
     * 并且所有的监听器收到后都会在主流程中阻塞执行
     */
    Broadcast,

    /**
     * 独播事件。 次事件只会被一个事件监听器处理。 并且这个监听器会阻塞执行
     */
    Solo;

}
