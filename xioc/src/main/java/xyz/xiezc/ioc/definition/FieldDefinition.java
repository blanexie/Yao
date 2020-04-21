package xyz.xiezc.ioc.definition;

import lombok.Data;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

@Data
public class FieldDefinition {

    BeanDefinition beanDefinition;

    /**
     * 字段的类型
     */
    Class<?> fieldType;

    /**
     * 字段的名称
     */
    String fieldName;

    /**
     * 如果这个字段有值， 说明这个字段是需要从容器中获取注入的
     * 如果这个字段是需要注入的字段，则这个就是需要注入beanName
     */
    String beanName;

    /**
     * 字段上的注解
     */
    AnnotatedElement annotatedElement;

    /**
     * 字段值
     */
    Object obj;


    @Override
    public String toString() {
        return "FieldDefinition{" +
                "beanDefinition=" + beanDefinition +
                ", fieldType=" + fieldType +
                ", fieldName='" + fieldName + '\'' +
                ", beanName='" + beanName + '\'' +
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
