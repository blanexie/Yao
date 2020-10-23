package xyz.xiezc.ioc.starter.core.context;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.setting.Setting;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.core.BeanCreateUtil;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanFactoryPostProcess;
import xyz.xiezc.ioc.starter.core.process.BeanPostProcess;
import xyz.xiezc.ioc.starter.exception.ManyBeanException;
import xyz.xiezc.ioc.starter.exception.NoSuchBeanException;

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

    /**
     * 初始化完成的 beanDefinition, 依赖已经注入了，初始化方法已经调用了
     */
    protected Map<Class<?>, BeanDefinition> singletonBeanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 正在创建的中bean， 主要用来解决循环依赖的问题
     */
    private Set<BeanDefinition> buildingSet = new HashSet<>();

    /**
     * 配置文件信息
     */
    @Setter
    @Getter
    protected Setting setting = new Setting();


    @Override
    public Map<Class<?>, BeanDefinition> getSingletonBeanDefinitionMap() {
        return singletonBeanDefinitionMap;
    }


    @Override
    public <T> T getBean(Class<?> clazz) {
        BeanDefinition beanDefinition = singletonBeanDefinitionMap.get(clazz);
        if (beanDefinition != null) {
            return beanDefinition.getBean();
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
            return beanDefinition1.getBean();
        }
        throw new NoSuchBeanException("容器中未找到符合要求的bean");
    }

    @Override
    public <T> List<T> getBeans(Class<?> clazz) {
        BeanCreateUtil instacne = BeanCreateUtil.getInstacne();
        List<T> result = new ArrayList<>();
        singletonBeanDefinitionMap.forEach((k, v) -> {
            if (ClassUtil.isAssignable(clazz, k)) {
                instacne.createAndInjectBean(v, this);
                result.add(v.getBean());
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

    }


    /**
     * 清除容器中的内容，方便重新启动
     */
    protected void clearContext() {
        singletonBeanDefinitionMap.clear();
        setting.clear();
        buildingSet.clear();
    }


    /**
     * 加载配置信息
     */
    protected void loadProperties() {
        URL resource = ResourceUtil.getResource("yao.properties");
        this.setting.addSetting(new Setting(resource, CharsetUtil.CHARSET_UTF_8, true));
    }

    /**
     * 扫描特定的目录结构，加载bean信息到容器中
     *
     * @param packageNames
     */
    void loadBeanDefinition(String... packageNames) {
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
     * 在正式启动之前进行一些操作， 这个方法一般留第三方扩展实现
     */
    abstract void beforeInvoke();

    /**
     * 调用所有的BeanFactoryPostProcess实现类的钩子方法
     * <p>
     * 会先实例化每个实现类(只是实例化，并不会注入依赖)， 再排序实现类， 最后逐个调用
     */
    protected void invokeBeanFactoryPostProcess() {
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
            for (BeanDefinition beanDefinition : beanFactoryPostProcessSet) {
                Class<?> beanClass = beanDefinition.getBeanClass();
                Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
                beanDefinition.setBean(bean);
                beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
            }
            //先排序BeanFactoryPostProcess的实现类, 再根据顺序逐个调用
            beanFactoryPostProcessSet.stream()
                    .sorted((a, b) -> {
                        BeanFactoryPostProcess bean = a.getBean();
                        BeanFactoryPostProcess bean1 = b.getBean();
                        int order = bean.order();
                        int order1 = bean1.order();
                        return order - order1;
                    }).forEachOrdered(beanDefinition -> {
                BeanFactoryPostProcess bean = beanDefinition.getBean();
                bean.process(this);
            });
        }
    }


    /**
     * 初始化BeanPostProcess的实现类， 这里是正常的初始化，会进行依赖注入和init方法的调用
     */
    protected void initBeanPostProcess() {
        List<BeanPostProcess> beanPostProcessList = new LinkedList<>();
        Collection<BeanDefinition> beanDefinitionCollection = singletonBeanDefinitionMap.values();
        //第一步， 先筛选出所有的BeanPostProcess 实现类
        Set<BeanDefinition> collect = beanDefinitionCollection.stream().filter(beanDefinition -> {
            Class<?> beanClass = beanDefinition.getBeanClass();
            boolean assignable = ClassUtil.isAssignable(BeanPostProcess.class, beanClass);
            return assignable;
        }).collect(Collectors.toSet());
        //逐个初始化BeanDefinition， 并调用其方法

        BeanCreateUtil beanCreateUtil = BeanCreateUtil.getInstacne();
        for (BeanDefinition beanDefinition : collect) {
            //调用BeanPost 前的钩子方法beforeInstance
            for (BeanPostProcess beanPostProcess : beanPostProcessList) {
                beanPostProcess.beforeInstance(this, beanDefinition);
            }
            //实例化
            beanCreateUtil.createBean(beanDefinition, this);
            //调用beforeInit方法
            for (BeanPostProcess beanPostProcess : beanPostProcessList) {
                beanPostProcess.beforeInit(this, beanDefinition);
            }
            //调用afterInit 方法
            for (BeanPostProcess beanPostProcess : beanPostProcessList) {
                beanPostProcess.afterInit(this, beanDefinition);
            }
        }

    }

    /**
     * 此处会加载事件分发器， 同时也会初始化事件监听器。
     * <p>
     * 会先查找容器中有无事件分发器， 如果没有则使用框架默认的事件分发器
     */
    void loadListener();


    /**
     * 初始化容器中所有的bean
     */
    void initAllBeans();

    /**
     * 最后结束的钩子方法
     */
    void finish();


}