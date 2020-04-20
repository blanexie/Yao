package xyz.xiezc.ioc.enums;

public enum BeanScopeEnum {
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
