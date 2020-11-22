package xyz.xiezc.ioc.starter.eventListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;


/**
 * 事件监听器中的事件对象
 */

@Getter
@ToString
public class ApplicationEvent {

    public ApplicationEvent(String eventName) {
        this.eventName = eventName;
    }

    /**
     * 事件的名称
     */
    private final String eventName;

    /**
     * 事件对象的生成时间戳
     */
    private final long timestamp = System.currentTimeMillis();

    /**
     * 事件的类型
     */
    @Setter
    private EventType eventType = EventType.Broadcast;

    /**
     * 事件锁携带的额外信息
     */
    private Map<String, Object> extra = new HashMap<>();


}
