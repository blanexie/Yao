package xyz.xiezc.ioc.common.event;

public interface ApplicationListener<E extends ApplicationEvent> {

    int order();

    void execute(E applicationEvent);

    String getEventName();

}