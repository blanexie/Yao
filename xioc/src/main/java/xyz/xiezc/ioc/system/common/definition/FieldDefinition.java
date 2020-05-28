package xyz.xiezc.ioc.system.common.definition;

import lombok.Data;
import xyz.xiezc.ioc.system.common.enums.FieldOrParamTypeEnum;

import java.lang.reflect.Field;
import java.util.Objects;

@Data
public class FieldDefinition {

    /**
     * 这个字段所在的bean
     */
    BeanDefinition beanDefinition;
    /**
     * 字段的类型。 如果字段是array或者Collecter类型。 则这里表示的是真实的类型
     */
    Class<?> fieldType;

    /**
     * 字段
     */
    Field field;

    /**
     * 字段的名称
     */
    String fieldName;

    /**
     * 1. 数组类型
     * 2. 集合类型
     * 其他. 正常类型
     */
    FieldOrParamTypeEnum fieldOrParamTypeEnum;

    /**
     * 字段值
     */
    Object fieldValue;


    @Override
    public String toString() {
        return "FieldDefinition{" +
                "beanDefinition=" + beanDefinition +
                ", fieldType=" + fieldType +
                ", fieldName='" + fieldName + '\'' +

                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldDefinition)) return false;
        FieldDefinition that = (FieldDefinition) o;
        return Objects.equals(getBeanDefinition(), that.getBeanDefinition()) &&
                Objects.equals(getFieldType(), that.getFieldType()) &&
                Objects.equals(getFieldName(), that.getFieldName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBeanDefinition(), getFieldType(), getFieldName());
    }
}
