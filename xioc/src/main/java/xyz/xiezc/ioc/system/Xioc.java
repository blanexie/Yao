package xyz.xiezc.ioc.system;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.system.annotation.Configuration;
import xyz.xiezc.ioc.system.event.ApplicationEvent;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;
import xyz.xiezc.ioc.system.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.system.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.system.common.enums.EventNameConstant;

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
    public static ApplicationContextUtil run(Class<?> clazz) {
        xioc = new Xioc();
        bootClass = clazz;

        //校验启动类上是否有@Configuration注解
        if (AnnotationUtil.getAnnotation(clazz, Configuration.class) == null) {
            throw new RuntimeException("请在启动类" + clazz.getName() + "上增加@Configuration注解");
        }

        ApplicationContextUtil applicationContextUtil = xioc.applicationContextUtil;

        //加载配置
        applicationContextUtil.getPropertiesContext().loadProperties();
        log.info("配置加载完成");


        //先扫描一次bean
        applicationContextUtil.scanBeanDefinitionClass();
        //框架特殊类想加入容器中， 并且初始化。
        // 1. 一些content基本类
        // 2. ComponentAnnotationHandler   和 ConfigurationAnnotationHandler






        //扫描所有的bean。 同时要注意configuration中配置的Bean注解和BeanScan注解


        //执行这里的扩展方法

        //开始初始化
        //1. 先特殊初始化类创建器
        //2. 初始化 事件处理器
        //3. 初始化普通类
        //4. 遍历容器中，重复1到3过程，直到所有bean都处理完成

        //执行结束后的扩展点


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


    /**
     *
     */
    public static BeanDefinition newInstance(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        beanDefinition.setBeanClass(clazz);
        beanDefinition.setBeanName(clazz.getName());
        beanDefinition.setBean(ReflectUtil.newInstanceIfPossible(clazz));
        return beanDefinition;
    }

}
