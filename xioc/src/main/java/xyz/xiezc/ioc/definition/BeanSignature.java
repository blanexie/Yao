package xyz.xiezc.ioc.definition;


import lombok.Data;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

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
    private BeanTypeEnum beanTypeEnum;

    /**
     * bean name
     */
    private String beanName;

    /**
     * bean的class
     */
    private Class<?> beanClass;

    @Override
    public String toString() {
        return "BeanSignature{" +
                "beanName='" + beanName + '\'' +
                ", beanClass=" + beanClass +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeanSignature)) return false;
        BeanSignature that = (BeanSignature) o;
        return getBeanName().equals(that.getBeanName()) &&
                getBeanClass().equals(that.getBeanClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBeanName(), getBeanClass());
    }
}
