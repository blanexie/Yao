package xyz.xiezc.ioc.definition;

import lombok.Data;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;


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
    String methodName;

    /**
     * 方法的参数
     */
    ParamDefinition[] paramDefinitions;


    @Override
    public String toString() {
        return "MethodDefinition{" +
                ", beanDefinition=" + beanDefinition +
                ", returnType=" + returnType +
                ", methodName='" + methodName + '\'' +
                ", paramDefinitions=" + Arrays.toString(paramDefinitions) +
                '}';
    }
}
