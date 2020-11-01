package xyz.xiezc.ioc.starter.core.definition;

import lombok.Data;

import java.lang.reflect.Parameter;

@Data
public class ParamDefinition {

    /**
     * 参数所属的方法
     */
    MethodDefinition methodDefinition;

    /**
     * 参数
     */
    Parameter parameter;
    /**
     * 参数名称
     */
    String paramName;

    /**
     * 具体的参数值
     */
    Object param;


}