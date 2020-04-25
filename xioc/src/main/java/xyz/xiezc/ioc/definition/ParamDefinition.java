package xyz.xiezc.ioc.definition;

import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;

@Data
public class ParamDefinition<T extends Annotation> {

    /**
     * 参数所属的方法
     */
    MethodDefinition methodDefinition;

    /**
     * 参数名称
     */
    String paramName;

    /**
     * 如果这个字段有值， 说明这个字段是需要从容器中获取注入的
     */
    String BeanName;
    /**
     * 参数类型
     */
    Class<?> paramType;
    /**
     * 参数的注解
     */
    AnnotatedElement annotatedElement;

    /**
     * 具体的参数值
     */
    Object param;

    @Override
    public String toString() {
        return "ParamDefinition{" +
                "methodDefinition=" + methodDefinition +
                ", paramName='" + paramName + '\'' +
                ", BeanName='" + BeanName + '\'' +
                ", paramType=" + paramType +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParamDefinition)) return false;
        ParamDefinition<?> that = (ParamDefinition<?>) o;
        return Objects.equals(getParamName(), that.getParamName()) &&
                Objects.equals(getParamType(), that.getParamType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParamName(), getParamType());
    }
}