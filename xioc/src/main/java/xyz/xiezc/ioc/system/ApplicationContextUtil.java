package xyz.xiezc.ioc.system;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import xyz.xiezc.ioc.system.annotation.AnnotationHandler;
import xyz.xiezc.ioc.system.annotation.Component;
import xyz.xiezc.ioc.system.annotation.Configuration;
import xyz.xiezc.ioc.system.annotation.handler.ComponentAnnotationHandler;
import xyz.xiezc.ioc.system.annotation.handler.ConfigurationAnnotationHandler;
import xyz.xiezc.ioc.system.common.context.*;
import xyz.xiezc.ioc.system.common.context.impl.*;
import xyz.xiezc.ioc.system.common.definition.BeanDefinition;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 容器，装载bean的容器,以及其他的所有的需要存放的对象都在这里
 */
@Data
public class ApplicationContextUtil {

    Log log = LogFactory.get(ApplicationContextUtil.class);

    /**
     * 全局线程池
     */
    public static ExecutorService executorService = ThreadUtil.newExecutor(1, 100);

    private BeanDefinitionContext beanDefinitionContext = new BeanDefinitionContextUtil();
    private AnnotationContext annotationContext = new AnnotationContextUtil();
    private PropertiesContext propertiesContext = new PropertiesContextUtil();
    private EventPublisherContext eventPublisherContext = new EventPublisherContextUtil();

    /**
     *
     */
    private BeanCreateContext beanCreateContext = new BeanCreateContextUtil(this);

    /**
     * 加载其他starter需要扫描的package路径
     */
    public final String starterPackage = "xyz.xiezc.ioc.starter";

    /**
     * 扫描本项目和所有starter中的所有容器类
     *
     * @param clazz
     */
    public void loadBeanDefinitions(Class<?> clazz) {
        //加载框架中的bean
        this.loadBeanDefinitions(ClassUtil.getPackage(clazz));
        //加载starter中的bean
        this.loadBeanDefinitions(starterPackage);
    }


    public void loadBeanDefinitions(String packagePath) {
        Set<Class<?>> classes = ClassUtil.scanPackage(packagePath);
        for (Class<?> aClass : classes) {
            this.loadBeanDefinition(aClass);
        }
    }


    /**
     * 加载某个具体的类BeanDefinition到容器中， 前提是这个类必须被@Component 和 Configuration注解
     */
    private void loadBeanDefinition(Class clazz) {
        //获取上面的component 注解
        Component component = AnnotationUtil.getAnnotation(clazz, Component.class);
        if (component != null) {
            BeanDefinition beanDefinition = beanDefinitionContext.getBeanDefinition(ComponentAnnotationHandler.class);
            AnnotationHandler annotationHandler = beanDefinition.getBean();
            annotationHandler.processClass(component, clazz, null);
        }

        Configuration configuration = AnnotationUtil.getAnnotation(clazz, Configuration.class);
        if (configuration != null) {
            BeanDefinition beanDefinition = beanDefinitionContext.getBeanDefinition(ConfigurationAnnotationHandler.class);
            AnnotationHandler annotationHandler = beanDefinition.getBean();
            annotationHandler.processClass(configuration, clazz, null);
        }
    }


}
