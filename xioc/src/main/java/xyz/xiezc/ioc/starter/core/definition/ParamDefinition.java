package xyz.xiezc.ioc.starter.core.definition;

import lombok.Data;
import xyz.xiezc.ioc.starter.common.enums.FieldOrParamTypeEnum;

import java.lang.reflect.Parameter;
import java.util.Objects;

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