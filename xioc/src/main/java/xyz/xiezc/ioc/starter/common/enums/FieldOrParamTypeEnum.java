package xyz.xiezc.ioc.starter.common.enums;

/**
 * 标识bean中字段的类型
 */
public enum FieldOrParamTypeEnum {
    /**
     * 配置字段
     */
    Properties,
    /**
     * 正常的注入的bean字段
     */
    Simple,
    /**
     * 数组字段
     */
    Array,
    /**
     * 集合类型的字段
     */
    Collection;
}
