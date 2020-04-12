package xyz.xiezc.ioc.common;


import cn.hutool.core.annotation.AnnotationUtil;
import lombok.Data;
import xyz.xiezc.ioc.AnnotationHandler;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理注解相关的工具类
 */
@Data
public class AnnoUtil {

    /**
     * 框架注解的路径
     */
    private String annoPath = "xyz.xiezc.ioc.annotationHandler";

    /**
     * 作用于类上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> classAnnoAndHandlerMap = new HashMap<>();
    /**
     * 作用于方法上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> methodAnnoAndHandlerMap = new HashMap<>();
    /**
     * 作用于字段上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> fieldAnnoAndHandlerMap = new HashMap<>();

    /**
     * 作用于注解上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> annoAndHandlerMap = new HashMap<>();

    /**
     * 作用于参数上面的注解
     */
    public Map<Class<? extends Annotation>, AnnotationHandler> parameterAnnoAndHandlerMap = new HashMap<>();

    /**
     * 增加一个注解处理器
     *
     * @param annotationHandler
     */
    public void addAnnotationHandler(AnnotationHandler annotationHandler) {
        //获取对应的注解
        Class<? extends Annotation> annotation = annotationHandler.getAnnotationType();
        ElementType[] value = AnnotationUtil.getTargetType(annotation);
        for (ElementType elementType : value) {
            //作用于类上
            if (elementType == ElementType.TYPE) {
                this.classAnnoAndHandlerMap.put(annotation, annotationHandler);
            }
            //作用于方法上面
            if (elementType == ElementType.METHOD) {
                this.methodAnnoAndHandlerMap.put(annotation, annotationHandler);
            }
            //作用于字段上面
            if (elementType == ElementType.FIELD) {
                this.fieldAnnoAndHandlerMap.put(annotation, annotationHandler);
            }

            //作用于参数上面
            if (elementType == ElementType.PARAMETER) {
                this.parameterAnnoAndHandlerMap.put(annotation, annotationHandler);
            }
            //作用于注解上面
            if (elementType == ElementType.PARAMETER) {
                this.annoAndHandlerMap.put(annotation, annotationHandler);
            }
        }
    }
}
