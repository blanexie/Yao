package xyz.xiezc.ioc.system.event;

import cn.hutool.core.lang.Dict;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ApplicationEvent {

    public ApplicationEvent(String eventName) {
        this.eventName = eventName;
    }

    private final String eventName;

    private final long timestamp = System.currentTimeMillis();

    public final long getTimestamp() {
        return this.timestamp;
    }

    private Dict dict;

}
