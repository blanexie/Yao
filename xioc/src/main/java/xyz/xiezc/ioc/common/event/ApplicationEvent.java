package xyz.xiezc.ioc.common.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public abstract class ApplicationEvent {

    public ApplicationEvent(String eventName) {
        this.eventName = eventName;
    }

    private final String eventName;

    private final long timestamp = System.currentTimeMillis();

    public final long getTimestamp() {
        return this.timestamp;
    }

}
