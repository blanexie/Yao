package xyz.xiezc.ioc.definition;

import lombok.Data;

import java.lang.reflect.AnnotatedElement;

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
     *
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
}
