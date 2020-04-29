package xyz.xiezc.ioc.definition;

import lombok.Data;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;


@Data
public class MethodDefinition {

    AnnotatedElement annotatedElement;

    /**
     * 方法所在的bean
     */
    private BeanDefinition beanDefinition;

    /**
     * 方法的返回类型
     */
    Class<?> returnType;

    /**
     * 方法的名称
     */
    Method method;

    /**
     * 方法的参数
     */
    ParamDefinition[] paramDefinitions;


    @Override
    public String toString() {
        return "MethodDefinition{" +
                "beanDefinition=" + beanDefinition +
                ", returnType=" + returnType +
                ", methodName='" + method + '\'' +
                ", paramDefinitions=" + Arrays.toString(paramDefinitions) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodDefinition)) return false;
        MethodDefinition that = (MethodDefinition) o;
        return Objects.equals(getBeanDefinition(), that.getBeanDefinition()) &&
                Objects.equals(getReturnType(), that.getReturnType()) &&
                Objects.equals(getMethod(), that.getMethod()) &&
                Arrays.equals(getParamDefinitions(), that.getParamDefinitions());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getBeanDefinition(), getReturnType(), getMethod());
        result = 31 * result + Arrays.hashCode(getParamDefinitions());
        return result;
    }
}
