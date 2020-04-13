package xyz.xiezc.ioc.common.event;

import cn.hutool.core.lang.Dict;
import lombok.Data;

import java.util.Objects;

@Data
public class Event {

    /**
     * 事件名称
     */
    public String name;

    /**
     *
     */
    public Dict dict;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return getName().equals(event.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
