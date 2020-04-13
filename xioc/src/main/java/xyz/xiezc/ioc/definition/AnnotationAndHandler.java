package xyz.xiezc.ioc.definition;

import lombok.Data;
import xyz.xiezc.ioc.AnnotationHandler;

import java.lang.annotation.Annotation;

@Data
public class AnnotationAndHandler {
    Annotation annotation;
    AnnotationHandler annotationHandler;
}