package xyz.xiezc.ioc;

import cn.hutool.core.util.ClassUtil;
import lombok.Data;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.enums.EventNameConstant;

import static xyz.xiezc.ioc.enums.EventNameConstant.loadBeanDefinition;

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
    private final ApplicationContextUtil applicationContextUtil = new ApplicationContextUtil();

    /**
     * 加载其他starter需要扫描的package路径
     */
    public final String starterPackage = "xyz.xiezc.ioc.starter";

    /**
     * 启动方法,
     *
     * @param clazz 传入的启动类, 以这个启动类所在目录为根目录开始扫描bean类
     */
    public static Xioc run(Class<?> clazz) {
        Xioc xioc = new Xioc();
        //开始启动框架
        BeanLoadUtil beanLoadUtil = xioc.applicationContextUtil.getBeanLoadUtil();
        xioc.applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.XiocStart));

        //加载配置
        beanLoadUtil.loadPropertie();
        //加载BeanDefinition， 主要加载框架中的，各个starter路径下的和传入的class路径下的BeanDefinition
        xioc.loadBeanDefinition(clazz, beanLoadUtil);

        //加载BeanFactoryUtil,并简单初始化bean创建器
        beanLoadUtil.loadBeanCreateStategy();
        //加载注解处理器， 并简单初始化bean
        beanLoadUtil.loadAnnotationHandler();
        //加载容器中的事件处理相关的bean
        beanLoadUtil.loadEventListener();
        xioc.scanBeanDefinition(beanLoadUtil);

        //注入依赖和初始化
        beanLoadUtil.initAndInjectBeans();
        xioc.applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.XiocEnd));
        return xioc;
    }

    /**
     * 扫描容器中的所有BeanDefinition的注解信息， 并处理
     *
     * @param beanLoadUtil
     */
    private void scanBeanDefinition(BeanLoadUtil beanLoadUtil) {
        //扫描容器中的bean， 处理所有在bean类上的注解
        beanLoadUtil.scanBeanDefinitionClass();
        //扫描容器中的bean，处理bean上的字段的自定义注解
        beanLoadUtil.scanBeanDefinitionField();
        //扫描容器中的bean, 处理方法
        beanLoadUtil.scanBeanDefinitionMethod();
    }

    /**
     * 三个加载逻辑放在一起
     *
     * @param clazz
     * @param beanLoadUtil
     */
    private void loadBeanDefinition(Class<?> clazz, BeanLoadUtil beanLoadUtil) {
        //加载框架中的bean
        beanLoadUtil.loadBeanDefinition(ClassUtil.getPackage(Xioc.class));
        //加载starter中的bean
        beanLoadUtil.loadBeanDefinition(starterPackage);
        //加载bean信息， 加载用户传入的地址的bean
        String packagePath = ClassUtil.getPackage(clazz);
        beanLoadUtil.loadBeanDefinition(packagePath);
        applicationContextUtil.publisherEvent(new ApplicationEvent(loadBeanDefinition));
    }

    public Xioc addApplicationListener(String eventName, ApplicationListener applicationListener) {
        applicationContextUtil.addApplicationListener(eventName, applicationListener);
        return this;
    }

}
