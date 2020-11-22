package xyz.xiezc.ioc.starter.eventListener;


import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 事件分发器
 */
public interface EventDispatcher {

    /**
     * 设置线程池
     */
    void setThreadPool(ExecutorService executorService);


    /**
     * 增加事件处理器
     */
    void addListener(ApplicationListener applicationListener);


    List<ApplicationListener> getListener(String eventName);



    void dispatcher(ApplicationEvent applicationEvent);

}
