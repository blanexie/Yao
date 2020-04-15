package xyz.xiezc.ioc.definition;

import lombok.Data;

import java.lang.annotation.Annotation;
import java.util.List;

@Data
public class ParamDefinition<T extends Annotation> {
    String name;
    Class<?> paramType;
    List<T> annotations;

    Object param;

}