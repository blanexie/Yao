package xyz.xiezc.ioc.common.event;

public interface Listener {

    int order();

    void execute(Event event);

    Event getEvent();

}