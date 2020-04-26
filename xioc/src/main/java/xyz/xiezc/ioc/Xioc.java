package xyz.xiezc.ioc;

import cn.hutool.core.util.ClassUtil;
import lombok.Data;
import xyz.xiezc.ioc.common.event.ApplicationEvent;

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
    private final ApplicationContextUtil contextUtil = new ApplicationContextUtil();

    /**
     * 扫描工具
     */
    private final BeanLoadUtil beanLoadUtil = new BeanLoadUtil(contextUtil);


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
    public Xioc run(Class<?> clazz) {
        //开始启动框架
        BeanLoadUtil beanLoadUtil = xioc.getBeanLoadUtil();

        //加载框架中的bean
        beanLoadUtil.loadBeanDefinition(ClassUtil.getPackage(Xioc.class));
        //加载starter中的bean
        beanLoadUtil.loadBeanDefinition(xioc.starterPackage);
        //加载bean信息， 加载用户传入的地址的bean
        String packagePath = ClassUtil.getPackage(clazz);
        beanLoadUtil.loadBeanDefinition(packagePath);

        //加载BeanFactoryUtil,并简单初始化bean创建器
        beanLoadUtil.loadBeanCreateStategy();
        //加载注解处理器， 并简单初始化bean
        beanLoadUtil.loadAnnotationHandler();
        //加载容器中的事件处理相关的bean
        beanLoadUtil.loadEventListener();

        //加载配置
        beanLoadUtil.loadPropertie();

        //扫描容器中的bean， 处理所有在bean类上的注解
        beanLoadUtil.scanBeanDefinitionClass();
        //扫描容器中的bean，处理bean上的字段的自定义注解
        beanLoadUtil.scanBeanDefinitionField();
        //扫描容器中的bean, 处理方法
        beanLoadUtil.scanBeanDefinitionMethod();
        //注入依赖和初始化
        beanLoadUtil.initAndInjectBeans();
        contextUtil.publisherEvent(new ApplicationEvent("initAndInjectBeans"));
        return xioc;
    }


}
