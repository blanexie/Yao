package xyz.xiezc.ioc.starter.eventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 事件分发器
 */
public interface EventDispatcher {


    List<ApplicationEvent> waitEvents = new ArrayList<>();

    /**
     * 增加事件处理器
     */
    void addListener(ApplicationListener applicationListener);


    List<ApplicationListener> getListener(String eventName);

    /**
     * 设置线程池
     */
    void setThreadPool(ExecutorService executorService);


    void execute(ApplicationEvent applicationEvent);

}
