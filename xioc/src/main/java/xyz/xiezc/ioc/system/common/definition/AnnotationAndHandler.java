package xyz.xiezc.ioc.system.common.definition;

import lombok.Data;
import xyz.xiezc.ioc.system.annotation.AnnotationHandler;

import java.lang.annotation.Annotation;

@Data
public class AnnotationAndHandler {
    Annotation annotation;
    AnnotationHandler annotationHandler;
}