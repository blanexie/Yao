package xyz.xiezc.ioc.starter.core.context;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.setting.Setting;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.core.BeanCreateUtil;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanFactoryPostProcess;
import xyz.xiezc.ioc.starter.core.process.BeanPostProcess;
import xyz.xiezc.ioc.starter.core.process.ConfigurationBeanFactoryPostProcess;
import xyz.xiezc.ioc.starter.eventListener.DefaultEventDispatcher;
import xyz.xiezc.ioc.starter.eventListener.EventDispatcher;
import xyz.xiezc.ioc.starter.exception.ManyBeanException;
import xyz.xiezc.ioc.starter.exception.NoSuchBeanException;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.common.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.eventListener.ApplicationEvent;
import xyz.xiezc.ioc.starter.eventListener.ApplicationListener;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 11:08 上午
 **/
public abstract class AbstractApplicationContext implements ApplicationContext {

    private String beanFactoryId = IdUtil.fastSimpleUUID();

    @Override
    public String beanFactoryId() {
        return beanFactoryId;
    }

    /**
     * 初始化完成的 beanDefinition, 依赖已经注入了，初始化方法已经调用了
     */
    @Getter
    protected Map<Class<?>, BeanDefinition> singletonBeanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 启动类
     */
    private Class appRunClass;

    /**
     * 配置文件信息
     */
    @Setter
    @Getter
    protected Setting setting = new Setting();

    /**
     * 事件处理器
     */
    @Getter
    EventDispatcher eventDispatcher;


    List<ApplicationContext> applicationContexts = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        applicationContexts.add(applicationContext);
    }

    @Override
    public ApplicationContext run(Class clazz) {
        this.appRunClass = clazz;
        //清除
        clearContext();
        addApplictionEvent(new ApplicationEvent(EventNameConstant.clearContextEvent));

        loadProperties();
        addApplictionEvent(new ApplicationEvent(EventNameConstant.loadPropertiesEvent));

        loadBeanDefinition("xyz.xiezc.ioc.starter", clazz.getPackageName());
        addApplictionEvent(new ApplicationEvent(EventNameConstant.loadBeanDefinitionEvent));

        beforeInvoke();
        addApplictionEvent(new ApplicationEvent(EventNameConstant.beforeInvokeEvent));

        invokeBeanFactoryPostProcess();
        addApplictionEvent(new ApplicationEvent(EventNameConstant.invokeBeanFactoryPostProcessEvent));

        initBeanPostProcess();
        addApplictionEvent(new ApplicationEvent(EventNameConstant.initBeanPostProcessEvent));

        initAllBeans();
        addApplictionEvent(new ApplicationEvent(EventNameConstant.initAllBeansEvent));

        loadListener();
        addApplictionEvent(new ApplicationEvent(EventNameConstant.loadListenerEvent));

        finish();
        addApplictionEvent(new ApplicationEvent(EventNameConstant.finishEvent));

        return this;
    }

    @Override
    public void addApplictionEvent(ApplicationEvent applicationEvent) {
        if (eventDispatcher == null) {
            EventDispatcher.waitEvents.add(applicationEvent);
        } else {
            eventDispatcher.execute(applicationEvent);
        }
    }

    @Override
    public BeanDefinition getBeanDefinition(Class<?> clazz) {
        BeanDefinition beanDefinition = singletonBeanDefinitionMap.get(clazz);
        if (beanDefinition != null) {
            return beanDefinition;
        }
        Set<Class<?>> collect = singletonBeanDefinitionMap.keySet().stream()
                .filter(clazzss -> ClassUtil.isAssignable(clazz, clazzss))
                .collect(Collectors.toSet());
        if (collect.isEmpty()) {
            throw new NoSuchBeanException("容器中未找到符合要求的bean:" + clazz.getName());
        }
        if (collect.size() > 1) {
            throw new ManyBeanException("容器中找到多个符合要求的bean" + clazz.getName());
        }
        for (Class<?> aClass : collect) {
            BeanDefinition beanDefinition1 = singletonBeanDefinitionMap.get(aClass);
            return beanDefinition1;
        }
        throw new NoSuchBeanException("容器中未找到符合要求的bean" + clazz.getName());
    }


    @Override
    public Collection<BeanDefinition> getBeanDefinitions(Class<?> clazz) {
        List<BeanDefinition> result = new ArrayList<>();
        singletonBeanDefinitionMap.forEach((k, v) -> {
            if (ClassUtil.isAssignable(clazz, k)) {
                result.add(v);
            }
        });
        if (result.isEmpty()) {
            throw new NoSuchBeanException("容器中未找到符合要求的bean");
        }
        return result;
    }


    /**
     * 清除容器中的内容，方便重新启动
     */
    @Override
    public void clearContext() {
        singletonBeanDefinitionMap.clear();
        setting.clear();
        BeanCreateUtil.getInstacne(this).clear();
        for (ApplicationContext applicationContext : applicationContexts) {
            applicationContext.clearContext();
        }
    }


    /**
     * 加载配置信息
     */
    @Override
    public void loadProperties() {
        URL resource = ResourceUtil.getResource("yao.properties");
        if (resource != null) {
            this.setting.addSetting(new Setting(resource, CharsetUtil.CHARSET_UTF_8, true));
        }
        for (ApplicationContext applicationContext : applicationContexts) {
            applicationContext.loadProperties();
        }
    }

    /**
     * 用于注解的注解情况
     */
    private Set<Class> annotationSet = new HashSet<>();

    /**
     * 扫描特定的目录结构，加载bean信息到容器中
     *
     * @param packageNames
     */
    @Override
    public void loadBeanDefinition(String... packageNames) {
        BeanDefinition rootBeanDefinition = new BeanDefinition();
        rootBeanDefinition.setBean(this);
        rootBeanDefinition.setBeanClass(this.getClass());
        rootBeanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        rootBeanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        this.addBean(rootBeanDefinition);

        for (String packageName : packageNames) {
            Set<Class<?>> classConfiguration = ClassUtil.scanPackageByAnnotation(packageName, Configuration.class);
            Set<Class<?>> classComponent = ClassUtil.scanPackageByAnnotation(packageName, Component.class);
            Set<Class<?>> componentSet = new HashSet<>();
            componentSet.addAll(classConfiguration);
            componentSet.addAll(classComponent);
            //筛选注解的注解
            List<Class> collect = classConfiguration.stream().filter(clazz -> ClassUtil.isAssignable(Annotation.class, clazz))
                    .collect(Collectors.toList());
            List<Class> collect1 = classComponent.stream().filter(clazz -> ClassUtil.isAssignable(Annotation.class, clazz))
                    .collect(Collectors.toList());

            annotationSet.addAll(collect);
            annotationSet.addAll(collect1);
            for (Class aClass : annotationSet) {
                componentSet.addAll(ClassUtil.scanPackageByAnnotation(packageName, aClass));
            }
            componentSet.stream()
                    .filter(clazz -> !ClassUtil.isAssignable(Annotation.class, clazz))
                    .forEach(clazz -> {
                        this.addBean(clazz);
                    });
        }

        for (ApplicationContext applicationContext : applicationContexts) {
            applicationContext.loadBeanDefinition(packageNames);
        }
    }


    @Override
    public void beforeInvoke() {
        for (ApplicationContext applicationContext : applicationContexts) {
            applicationContext.beforeInvoke();
        }
    }

    @Override
    public Class getAppRunClass() {
        return appRunClass;
    }

    /**
     * 调用所有的BeanFactoryPostProcess实现类的钩子方法
     * <p>
     * 会先实例化每个实现类(只是实例化，并不会注入依赖)， 再排序实现类， 最后逐个调用
     */
    @Override
    public void invokeBeanFactoryPostProcess() {
        invokeConfigurationProcess();
        Set<BeanDefinition> beanFactoryPostProcessSet = new HashSet<>();
        while (true) {
            //第一步, 筛选出来所有的BeanFactoryPostProcess的实现类
            List<BeanDefinition> collect = singletonBeanDefinitionMap.values().stream()
                    .filter(beanDefinition -> beanDefinition.getBean() != ConfigurationBeanFactoryPostProcess.class)
                    .filter(beanDefinition -> !beanFactoryPostProcessSet.contains(beanDefinition))
                    .filter(beanDefinition -> {
                        Class<?> beanClass = beanDefinition.getBeanClass();
                        return ClassUtil.isAssignable(BeanFactoryPostProcess.class, beanClass);
                    })
                    .collect(Collectors.toList());
            // .forEach(beanDefinition -> beanFactoryPostProcessSet.add(beanDefinition));
            if (collect.isEmpty()) {
                //跳出循环
                break;
            }
            //直接反射生成对象。
            Map<BeanDefinition, BeanFactoryPostProcess> beanFactoryPostProcessMap = new HashMap<>();
            for (BeanDefinition beanDefinition : collect) {
                if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
                    Class<?> beanClass = beanDefinition.getBeanClass();
                    Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
                    beanDefinition.setBean(bean);
                    beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
                    beanFactoryPostProcessMap.put(beanDefinition, (BeanFactoryPostProcess) bean);
                }
            }
            //先排序BeanFactoryPostProcess的实现类, 再根据顺序逐个调用
            collect.stream()
                    .sorted((a, b) -> {
                        BeanFactoryPostProcess bean = a.getBean();
                        BeanFactoryPostProcess bean1 = b.getBean();
                        int order = bean.order();
                        int order1 = bean1.order();
                        return order - order1;
                    })
                    .forEachOrdered(beanDefinition -> {
                        BeanFactoryPostProcess beanFactoryPostProcess = beanDefinition.getBean();
                        beanFactoryPostProcess.process(this);
                        beanFactoryPostProcessSet.add(beanDefinition);
                    });
        }


        for (ApplicationContext applicationContext : applicationContexts) {
            applicationContext.invokeBeanFactoryPostProcess();
        }
    }

    void invokeConfigurationProcess() {
        BeanDefinition beanDefinition = getBeanDefinition(ConfigurationBeanFactoryPostProcess.class);
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
        beanDefinition.setBean(bean);
        beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        BeanFactoryPostProcess beanFactoryPostProcess = beanDefinition.getBean();
        beanFactoryPostProcess.process(this);
    }


    /**
     * 初始化BeanPostProcess的实现类， 这里是正常的初始化，会进行依赖注入和init方法的调用
     */
    @Override
    public void initBeanPostProcess() {
        Collection<BeanDefinition> beanDefinitionCollection = singletonBeanDefinitionMap.values();
        //第一步， 先筛选出所有的BeanPostProcess 实现类
        Set<BeanDefinition> collect = beanDefinitionCollection.stream().filter(beanDefinition -> {
            Class<?> beanClass = beanDefinition.getBeanClass();
            boolean assignable = ClassUtil.isAssignable(BeanPostProcess.class, beanClass);
            return assignable;
        }).collect(Collectors.toSet());
        //逐个初始化BeanDefinition， 并调用其方法
        BeanCreateUtil beanCreateUtil = BeanCreateUtil.getInstacne(this);
        PriorityQueue<BeanPostProcess> beanPostProcessPriorityQueue = beanCreateUtil.getBeanPostProcessPriorityQueue();
        for (BeanDefinition beanDefinition : collect) {
            beanCreateUtil.createAndInject(beanDefinition);
            beanPostProcessPriorityQueue.add(beanDefinition.getBean());
        }

        for (ApplicationContext applicationContext : applicationContexts) {
            applicationContext.initBeanPostProcess();
        }
    }

    /**
     * 此处会加载事件分发器， 同时也会初始化事件监听器。
     * <p>
     * 会先查找容器中有无事件分发器， 如果没有则使用框架默认的事件分发器
     */
    @Override
    public void loadListener() {
        List<ApplicationListener> applicationListener = new ArrayList<>();
        BeanCreateUtil instacne = BeanCreateUtil.getInstacne(this);

        try {
            Collection<BeanDefinition> beanDefinitions = this.getBeanDefinitions(ApplicationListener.class);
            for (BeanDefinition beanDefinition : beanDefinitions) {
                instacne.createAndInject(beanDefinition);
                applicationListener.add(beanDefinition.getCompletedBean());
            }
        } catch (NoSuchBeanException e) {

        }

        try {
            BeanDefinition beanDefinition = this.getBeanDefinition(EventDispatcher.class);
            instacne.createAndInject(beanDefinition);
            eventDispatcher = beanDefinition.getCompletedBean();
        } catch (NoSuchBeanException e) {
            eventDispatcher = new DefaultEventDispatcher();
            eventDispatcher.setThreadPool(ThreadUtil.newExecutor(5));
        }

        for (ApplicationListener listener : applicationListener) {
            eventDispatcher.addListener(listener);
        }
        for (ApplicationContext applicationContext : applicationContexts) {
            applicationContext.loadListener();
        }
    }


    /**
     * 初始化容器中所有的bean
     */
    @Override
    public void initAllBeans() {
        BeanCreateUtil instacne = BeanCreateUtil.getInstacne(this);
        getSingletonBeanDefinitionMap().forEach((k, v) -> {
            instacne.createAndInject(v);
        });

        //逐个调用初始化方法
        getSingletonBeanDefinitionMap().forEach((k, v) -> {
            instacne.initAndComplete(v);
        });

        for (ApplicationContext applicationContext : applicationContexts) {
            applicationContext.initAllBeans();
        }
    }


    @Override
    public String getProperty(String propertyName) {
        return setting.getStr(propertyName);
    }


    @Override
    public Properties getProperties() {
        return setting.toProperties();
    }

    @Override
    public void addBean(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(clazz);
        beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        beanDefinition.setBeanStatus(BeanStatusEnum.Original);
        this.getSingletonBeanDefinitionMap().put(clazz, beanDefinition);
    }

    @Override
    public void addBean(BeanDefinition beanDefinition) {
        Assert.notNull(beanDefinition.getBeanClass());
        Assert.notNull(beanDefinition.getBeanStatus());
        Assert.notNull(beanDefinition.getBeanTypeEnum());
        this.getSingletonBeanDefinitionMap().put(beanDefinition.getBeanClass(), beanDefinition);
    }

    @Override
    public void finish() {
        for (ApplicationContext applicationContext : applicationContexts) {
            applicationContext.finish();
        }
    }
}