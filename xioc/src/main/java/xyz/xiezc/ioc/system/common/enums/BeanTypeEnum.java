package xyz.xiezc.ioc.system.common.enums;

public enum BeanTypeEnum {
    /**
     * 普通的bean， 单例
     */
    bean,

    /**
     * factoryBean
     */
    factoryBean,

    /**
     * methodBean
     */
    methodBean,

    /**
     * 使用method的方式生成的bean
     */
    properties;
}
