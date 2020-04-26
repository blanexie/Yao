package xyz.xiezc.ioc.annotation;

import xyz.xiezc.ioc.annotation.handler.BeanScanAnnotationHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 放入容器
 * <p>
 * 指示扫描加载路径的注解
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanScan {

    /**
     * 扫描的包路径
     *
     * @return
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages} for specifying the packages
     * to scan for annotated components. The package of each class specified will be scanned.
     * <p>Consider creating a special no-op marker class or interface in each package
     * that serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};


    Class<? extends AnnotationHandler> annotationHandler = BeanScanAnnotationHandler.class;


}
