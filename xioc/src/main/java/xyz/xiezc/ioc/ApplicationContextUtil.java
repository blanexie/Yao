package xyz.xiezc.ioc;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import lombok.Data;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.common.context.*;
import xyz.xiezc.ioc.common.context.impl.*;
import xyz.xiezc.ioc.common.create.BeanCreateStrategy;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;
import xyz.xiezc.ioc.enums.EventNameConstant;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 容器，装载bean的容器,以及其他的所有的需要存放的对象都在这里
 */
@Data
public class ApplicationContextUtil {

    Log log = LogFactory.get(ApplicationContextUtil.class);

    private final BeanLoadUtil beanLoadUtil = new BeanLoadUtil(this);

    /**
     * 全局线程池
     */
    public static ExecutorService executorService = ThreadUtil.newExecutor(1, 100);

    private BeanDefinitionContext beanDefinitionContext = new BeanDefinitionContextUtil();

    private AnnotationContext annotationContext = new AnnotationContextUtil();

    private BeanCreateContext beanCreateContext = new BeanCreateContextUtil(beanDefinitionContext);

    private PropertiesContext propertiesContext = new PropertiesContextUtil();

    private EventPublisherContext eventPublisherContext = new EventPublisherContextUtil();



    /**
     * 加载bean初始化类
     */
    public void loadBeanCreateStategy() {
        List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(BeanCreateStrategy.class);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanDefinition = applicationContextUtil.newInstance(beanDefinition);
            BeanCreateStrategy bean = beanDefinition.getBean();
            bean.setApplicationContext(applicationContextUtil);
            applicationContextUtil.putBeanCreateStrategy(bean);
        }
        applicationContextUtil.publisherEvent(new ApplicationEvent(EventNameConstant.loadBeanCreateStategy));

    }





}
