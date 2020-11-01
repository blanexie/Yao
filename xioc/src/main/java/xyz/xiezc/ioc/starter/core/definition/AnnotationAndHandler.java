package xyz.xiezc.ioc.starter.core.definition;

import lombok.Data;

import java.lang.annotation.Annotation;

@Data
public class AnnotationAndHandler {
    Annotation annotation;
    AnnotationHandler annotationHandler;
}