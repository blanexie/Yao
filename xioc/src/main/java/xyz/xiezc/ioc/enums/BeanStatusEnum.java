package xyz.xiezc.ioc.enums;

import lombok.Getter;

@Getter
public enum BeanStatusEnum {

    /**
     * 初始状态
     */
    Original,
    /**
     * 已经生成对象， 但是还没有注入依赖的
     */
    HalfCooked,
    /**
     * 全部完成的
     */
    Completed;

}
