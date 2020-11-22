package xyz.xiezc.ioc.starter.common.enums;

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
     * 注入了所有的依赖字段
     */
    Injected,

    /**
     * 完成了初始化方法的调用
     */
    Inited,

    /**
     * 全部完成的， 初始化后的钩子方法也调用了的
     */
    Completed;

}
