package xyz.xiezc.ioc;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.annotation.Configuration;
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

    static Log log = LogFactory.get(Xioc.class);

    /**
     * 装载依赖的容器. 当依赖全部注入完成的时候,这个集合会清空
     */
    private final ApplicationContextUtil applicationContextUtil = new ApplicationContextUtil();

    /**
     * 加载其他starter需要扫描的package路径
     */
    public final String starterPackage = "xyz.xiezc.ioc.starter";

    static Xioc xioc;

    public static Class<?> bootClass;

    /**
     * 启动方法,
     *
     * @param clazz 传入的启动类, 以这个启动类所在目录为根目录开始扫描bean类
     */
    public static Xioc run(Class<?> clazz) {
        xioc = new Xioc();
        bootClass = clazz;

        //校验启动类上是否有@Configuration注解
        if (AnnotationUtil.getAnnotation(clazz, Configuration.class) == null) {
            throw new RuntimeException("请在启动类" + clazz.getName() + "上增加@Configuration注解");
        }

        ApplicationContextUtil applicationContextUtil = xioc.applicationContextUtil;

        //开始启动框架
        BeanLoadUtil beanLoadUtil = applicationContextUtil.getBeanLoadUtil();
        xioc.applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.XiocStart));

        //加载配置
        applicationContextUtil.getPropertiesContext().loadProperties();
        log.info("配置加载完成");


        //加载BeanDefinition， 主要加载框架中的，各个starter路径下的和传入的class路径下的BeanDefinition
        xioc.loadBeanDefinition(clazz, beanLoadUtil);
        //加载BeanFactoryUtil,并简单初始化bean创建器
        beanLoadUtil.loadBeanCreateStategy();
        //加载注解处理器
        beanLoadUtil.loadAnnotationHandler();
        //加载容器中的事件处理相关的bean
        beanLoadUtil.loadEventListener();
        log.info("xioc的事件处理器加载完成");
        xioc.scanBeanDefinition(beanLoadUtil);
        log.info("xioc的BeanDefinition加载完成");

        //注入依赖和初始化
        beanLoadUtil.initAndInjectBeans();
        log.info("xioc的bean初始化完成");
        xioc.applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.XiocEnd));
        log.info("xioc 容器加载完成");
        return xioc;
    }

    public static ApplicationContextUtil getApplicationContext() {
        return xioc.getApplicationContextUtil();
    }



}
