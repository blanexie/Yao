package xyz.xiezc.ioc;

import lombok.Data;
import xyz.xiezc.ioc.common.BeanScanUtil;
import xyz.xiezc.ioc.common.ContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.definition.BeanTypeEnum;
import xyz.xiezc.ioc.test.TestB;
import xyz.xiezc.ioc.test.TestC;

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

    public static void main(String[] args) {
        Xioc.run(Xioc.class);

        BeanSignature beanSignature = new BeanSignature();
        beanSignature.setBeanClass(TestC.class);
        beanSignature.setBeanTypeEnum(BeanTypeEnum.bean);
        beanSignature.setBeanName("testc");

        BeanDefinition beanDefinition = contextUtil.getComplatedBeanDefinitionBySignature(beanSignature);
        TestC bean = (TestC) beanDefinition.getBean();
        bean.print();
    }

    /**
     * 装载依赖的容器. 当依赖全部注入完成的时候,这个集合会清空
     */
    private static final ContextUtil contextUtil = new ContextUtil();

    /**
     * 扫描工具
     */
    private static final BeanScanUtil beanScanUtil = new BeanScanUtil(contextUtil);


    /**
     * 启动方法
     *
     * @param clazz 传入的启动类
     */
    public static void run(Class<?> clazz) {
        //加载配置
        beanScanUtil.loadPropertie();
        //加载注解信息
        beanScanUtil.loadAnnotation();



        //加载bean信息
        beanScanUtil.loadBeanDefinition(clazz);


        //扫描容器中的bean， 处理所有在bean类上的注解
        beanScanUtil.scanClass();
        //扫描容器中的bean，处理bean上的字段的自定义注解
        beanScanUtil.scanField();
        //注入依赖和初始化
        beanScanUtil.initAndInjectBeans();
        //扫描容器中的bean, 处理方法
        beanScanUtil.scanMethod();
    }

    /**
     * 自动添加注解处理器， 可以使用这个特性自定义注解
     *
     * @param annotationHandler
     */
    public static void addAnnoHandler(AnnotationHandler<? extends Annotation> annotationHandler) {
        contextUtil.getAnnoUtil().addAnnotationHandler(annotationHandler);
    }

}
