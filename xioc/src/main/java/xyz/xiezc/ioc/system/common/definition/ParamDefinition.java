package xyz.xiezc.ioc.system.common.definition;

import lombok.Data;
import xyz.xiezc.ioc.system.common.enums.FieldOrParamTypeEnum;

import java.util.Objects;

@Data
public class ParamDefinition{

    /**
     * 参数所属的方法
     */
    MethodDefinition methodDefinition;

    /**
     * 如果这个字段有值， 说明这个字段是需要从容器中获取注入的
     */
    FieldOrParamTypeEnum fieldOrParamTypeEnum;

    /**
     * 参数名称
     */
    String paramName;

    /**
     * 参数类型
     */
    Class<?> paramType;

    /**
     * 具体的参数值
     */
    Object param;


    @Override
    public String toString() {
        return "ParamDefinition{" +
                "methodDefinition=" + methodDefinition +
                ", paramName='" + paramName + '\'' +
                ", paramType=" + paramType +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParamDefinition)) return false;
        ParamDefinition that = (ParamDefinition) o;
        return Objects.equals(getParamName(), that.getParamName()) &&
                Objects.equals(getParamType(), that.getParamType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParamName(), getParamType());
    }
}