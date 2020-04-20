package xyz.xiezc.ioc.definition;


import lombok.Data;
import xyz.xiezc.ioc.enums.BeanScopeEnum;

import java.util.Objects;

/**
 * bean 的 签名信息， 唯一确定一个bean的两个信息。
 * <p>
 * 目前的bean有两种类型，
 * 一种是正常的bean
 * 一种是配置信息
 */
@Data
public class BeanSignature {
    /**
     * bean ： 类上的注解，放入容器的bean
     * properties ： 配置的注入
     */
    private BeanScopeEnum beanScopeEnum;

    /**
     * 是否是单例模式。 默认是单例的
     */
    private boolean isSingleton = true;


    /**
     * bean paramName
     */
    private String beanName;

    /**
     * bean的class
     */
    private Class<?> beanClass;



}
