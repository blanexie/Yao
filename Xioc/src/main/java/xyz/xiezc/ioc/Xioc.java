package xyz.xiezc.ioc;

import cn.hutool.core.util.ClassUtil;
import lombok.Data;
import xyz.xiezc.ioc.common.BeanScanUtil;
import xyz.xiezc.ioc.common.ContextUtil;

import java.lang.annotation.Annotation;

/**
 * 超级简单的依赖注入小框架
 * <p>
 * 1. 不支持注解的注解
 * 2. 目前只支持单例的bean
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:17
 */
@Data
public final class Xioc {

    /**
     * 装载依赖的容器. 当依赖全部注入完成的时候,这个集合会清空
     */
    private final ContextUtil contextUtil = new ContextUtil();

    /**
     * 扫描工具
     */
    private final BeanScanUtil beanScanUtil = new BeanScanUtil(contextUtil);

    /**
     * 加载其他starter需要扫描的package路径
     */
    public final String starterPackage = "xyz.xiezc.ioc.starter";

    /**
     * 单例模式
     */
    private static Xioc xioc = new Xioc();

    /**
     *
     */
    private Xioc() {
    }

    /**
     * 单例模式的获取
     *
     * @return
     */
    public static Xioc getSingleton() {
        return xioc;
    }

    /**
     * 启动方法,
     *
     * @param clazz 传入的启动类, 以这个启动类所在目录为根目录开始扫描bean类
     */
    public static Xioc run(Class<?> clazz) {

        BeanScanUtil beanScanUtil = xioc.getBeanScanUtil();
        //加载配置
        beanScanUtil.loadPropertie();
        //加载注解信息
        beanScanUtil.loadAnnotation();

        //加载starter中的bean
        beanScanUtil.loadBeanDefinition(xioc.starterPackage);

        //加载bean信息
        String packagePath = ClassUtil.getPackage(clazz);
        beanScanUtil.loadBeanDefinition(packagePath);

        //扫描容器中的bean， 处理所有在bean类上的注解
        beanScanUtil.scanClass();
        //扫描容器中的bean，处理bean上的字段的自定义注解
        beanScanUtil.scanField();
        //注入依赖和初始化
        beanScanUtil.initAndInjectBeans();
        //扫描容器中的bean, 处理方法
        beanScanUtil.scanMethod();
        return xioc;
    }

    /**
     * 自动添加注解处理器， 可以使用这个特性自定义注解
     *
     * @param annotationHandler
     */
    public static Xioc addAnnoHandler(AnnotationHandler<? extends Annotation> annotationHandler) {
        xioc.contextUtil.getAnnoUtil().addAnnotationHandler(annotationHandler);
        return xioc;
    }

}
