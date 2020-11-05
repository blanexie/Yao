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
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.core.BeanCreateUtil;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanFactoryPostProcess;
import xyz.xiezc.ioc.starter.core.process.BeanPostProcess;
import xyz.xiezc.ioc.starter.event.ApplicationListener;
import xyz.xiezc.ioc.starter.event.DefaultEventDispatcher;
import xyz.xiezc.ioc.starter.event.EventDispatcher;
import xyz.xiezc.ioc.starter.exception.ManyBeanException;
import xyz.xiezc.ioc.starter.exception.NoSuchBeanException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
     * 配置文件信息
     */
    @Setter
    @Getter
    protected Setting setting = new Setting();

    /**
     * 时间处理器
     */
    @Getter
    EventDispatcher eventDispatcher;


    @Override
    public <T> T getBean(Class<?> clazz) {
        BeanDefinition beanDefinition = singletonBeanDefinitionMap.get(clazz);
        if (beanDefinition != null) {
            if (beanDefinition.getBeanStatus() != BeanStatusEnum.Completed) {
                BeanCreateUtil.getInstacne(this).createAndInitBeanDefinition(beanDefinition);
            }
            return beanDefinition.getCompletedBean();
        }
        Set<Class<?>> collect = singletonBeanDefinitionMap.keySet().stream()
                .filter(clazzss -> ClassUtil.isAssignable(clazz, clazzss))
                .collect(Collectors.toSet());
        if (collect.isEmpty()) {
            throw new NoSuchBeanException("容器中未找到符合要求的bean");
        }
        if (collect.size() > 1) {
            throw new ManyBeanException("容器中找到多个符合要求的bean");
        }
        for (Class<?> aClass : collect) {
            BeanDefinition beanDefinition1 = singletonBeanDefinitionMap.get(aClass);
            return beanDefinition1.getCompletedBean();
        }
        throw new NoSuchBeanException("容器中未找到符合要求的bean");
    }

    @Override
    public <T> List<T> getBeans(Class<?> clazz) {
        BeanCreateUtil instacne = BeanCreateUtil.getInstacne(this);
        List<T> result = new ArrayList<>();
        singletonBeanDefinitionMap.forEach((k, v) -> {
            if (ClassUtil.isAssignable(clazz, k)) {
                instacne.createAndInitBeanDefinition(v);
                result.add(v.getCompletedBean());
            }
        });
        if (result.isEmpty()) {
            throw new NoSuchBeanException("容器中未找到符合要求的bean");
        }
        return result;
    }

    @Override
    public ApplicationContext run(Class clazz) {
        //清除
        clearContext();
        //
        loadProperties();
        loadBeanDefinition(clazz.getPackageName());
        beforeInvoke();
        invokeBeanFactoryPostProcess();
        initBeanPostProcess();

        loadListener();

        initAllBeans();

        finish();

        return this;
    }


    /**
     * 清除容器中的内容，方便重新启动
     */
    @Override
    public void clearContext() {
        singletonBeanDefinitionMap.clear();
        setting.clear();
        BeanCreateUtil.getInstacne(this).clear();
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
    }

    /**
     * 扫描特定的目录结构，加载bean信息到容器中
     *
     * @param packageNames
     */
    @Override
    public void loadBeanDefinition(String... packageNames) {
        for (String packageName : packageNames) {
            Set<Class<?>> classConfiguration = ClassUtil.scanPackageByAnnotation(packageName, Configuration.class);
            Set<Class<?>> classComponent = ClassUtil.scanPackageByAnnotation(packageName, Component.class);
            classConfiguration.stream().forEach(clazz -> {
                this.addBean(clazz);
            });
            classComponent.stream().forEach(clazz -> {
                this.addBean(clazz);
            });
        }
    }


    /**
     * 调用所有的BeanFactoryPostProcess实现类的钩子方法
     * <p>
     * 会先实例化每个实现类(只是实例化，并不会注入依赖)， 再排序实现类， 最后逐个调用
     */
    @Override
    public void invokeBeanFactoryPostProcess() {
        while (true) {
            //第一步, 筛选出来所有的BeanFactoryPostProcess的实现类
            Set<BeanDefinition> beanFactoryPostProcessSet = singletonBeanDefinitionMap.values().stream()
                    .filter(beanDefinition -> beanDefinition.getBeanStatus() == BeanStatusEnum.Original)
                    .filter(beanDefinition -> {
                        Class<?> beanClass = beanDefinition.getBeanClass();
                        return ClassUtil.isAssignable(BeanFactoryPostProcess.class, beanClass);
                    }).collect(Collectors.toSet());
            if (beanFactoryPostProcessSet.isEmpty()) {
                return;
            }
            //直接反射生成对象。
            Map<BeanDefinition, BeanFactoryPostProcess> beanFactoryPostProcessMap = new HashMap<>();
            for (BeanDefinition beanDefinition : beanFactoryPostProcessSet) {
                Class<?> beanClass = beanDefinition.getBeanClass();
                Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
                beanDefinition.setBean(bean);
                beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
                beanFactoryPostProcessMap.put(beanDefinition, (BeanFactoryPostProcess) bean);
            }
            //先排序BeanFactoryPostProcess的实现类, 再根据顺序逐个调用
            beanFactoryPostProcessSet.stream()
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
                    });
        }
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
        for (BeanDefinition beanDefinition : collect) {
            beanCreateUtil.createAndInitBeanDefinition(beanDefinition);
        }

    }

    /**
     * 此处会加载事件分发器， 同时也会初始化事件监听器。
     * <p>
     * 会先查找容器中有无事件分发器， 如果没有则使用框架默认的事件分发器
     */
    @Override
    public void loadListener() {
        List<ApplicationListener> applicationListener;
        try {
            applicationListener = this.getBeans(ApplicationListener.class);
        } catch (NoSuchBeanException e) {
            applicationListener = new ArrayList<>(0);
        }
        try {
            eventDispatcher = this.getBean(EventDispatcher.class);
        } catch (NoSuchBeanException e) {
            eventDispatcher = new DefaultEventDispatcher();
            eventDispatcher.setThreadPool(ThreadUtil.newExecutor(5));
        }

        for (ApplicationListener listener : applicationListener) {
            eventDispatcher.addListener(listener);
        }
    }


    /**
     * 初始化容器中所有的bean
     */
    @Override
    public void initAllBeans() {
        BeanCreateUtil instacne = BeanCreateUtil.getInstacne(this);
        getSingletonBeanDefinitionMap().forEach((k, v) -> {
            instacne.createAndInitBeanDefinition(v);
        });
    }



    @Override
    public String getProperty(String propertyName) {
        return setting.getStr(propertyName);
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

    private <T> T covert(String value, Class<T> clazz) {
        try {
            return Convert.convert(clazz, value);
        } catch (ConvertException e) {
            throw e;
        }
    }


    private boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    private boolean isCollection(Class<?> clazz) {
        return ClassUtil.isAssignable(Collection.class, clazz);
    }
}