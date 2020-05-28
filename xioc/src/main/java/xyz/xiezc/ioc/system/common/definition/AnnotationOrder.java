package xyz.xiezc.ioc.system.common.definition;

import lombok.Data;

import java.lang.annotation.Annotation;


@Data
public abstract class AnnotationOrder implements Comparable<AnnotationOrder> {

    /**
     * 对应的注解类型
     */
    Class<? extends Annotation> annotationType;

    /**
     * 优先级最高
     * 必须在这个注解之后
     */
    Class<? extends Annotation> afterTo;

    /**
     * 优先级期次
     * 必须在这个注解之前
     */
    Class<? extends Annotation> beforeTo;

    /**
     * 优先级最低
     * 序号
     */
    int order = 100;


    @Override
    public int compareTo(AnnotationOrder o) {
        if (this.afterTo == o.getAnnotationType()) {
            return -1;
        }
        if (this.beforeTo == o.getAnnotationType()) {
            return 1;
        }

        return this.order - o.getOrder();
    }
}
