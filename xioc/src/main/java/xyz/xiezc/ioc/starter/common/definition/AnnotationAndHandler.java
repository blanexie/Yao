package xyz.xiezc.ioc.starter.common.definition;

import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.AnnotationHandler;

import java.lang.annotation.Annotation;

@Data
public class AnnotationAndHandler {
    Annotation annotation;
    AnnotationHandler annotationHandler;
}