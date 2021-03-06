package xyz.xiezc.ioc.starter.event;

import xyz.xiezc.ioc.starter.ApplicationContextUtil;

public interface ApplicationListener {

    /**
     * 事件的排序
     *
     * @return
     */
    int order();

    /**
     * 时间的处理逻辑
     *
     * @param applicationEvent
     */
    void doExecute(ApplicationEvent applicationEvent);

    /**
     * 事件的前置处理逻辑
     *
     * @param applicationEvent
     */
    default void execute(ApplicationEvent applicationEvent) {
        if (getSync()) {
            doExecute(applicationEvent);
        } else {
            ApplicationContextUtil.executorService.execute(() -> {
                doExecute(applicationEvent);
            });
        }
    }


    /**
     * 是否同步调用事件， 默认是的
     *
     * @return
     */
    default boolean getSync() {
        return true;
    }

}