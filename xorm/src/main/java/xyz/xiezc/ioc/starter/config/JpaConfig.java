package xyz.xiezc.ioc.starter.config;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.extra.cglib.CglibUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.internal.PersistenceUnitUtilImpl;
import org.hibernate.service.ServiceRegistry;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Component;
import xyz.xiezc.ioc.starter.common.asm.AsmUtil;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.common.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.eventListener.ApplicationEvent;
import xyz.xiezc.ioc.starter.eventListener.ApplicationListener;
import xyz.xiezc.ioc.starter.orm.annotation.Query;
import xyz.xiezc.ioc.starter.orm.annotation.Repository;
import xyz.xiezc.ioc.starter.orm.core.BasicRepository;
import xyz.xiezc.ioc.starter.orm.core.HibernateUtil;
import xyz.xiezc.ioc.starter.orm.core.JpaInvocationHandler;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiezc
 */
public class JpaConfig implements ApplicationContext {


    ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public ApplicationContext run(Class clazz) {
        return null;
    }

    @Override
    public void addApplictionEvent(ApplicationEvent applicationEvent) {
    }

    @Override
    public void clearContext() {
    }

    @Override
    public void loadProperties() {
    }

    @Override
    public void loadBeanDefinition(String... packageNames) {
        //获取
        String repositoryPackage = applicationContext.getProperty("jpa.repository.package");
        if (StrUtil.isNotEmpty(repositoryPackage)) {
            //扫描到的接口
            scanRepository(repositoryPackage);
        } else {
            //扫描到的接口
            scanRepository(packageNames);
        }
    }

    /**
     * 扫描Repository 注解的类到容器中
     *
     * @param repositoryPackages
     */
    private void scanRepository(String... repositoryPackages) {
        Properties properties = applicationContext.getProperties();
        Set<Class<?>> classSet = new HashSet<>();
        for (String repositoryPackage : repositoryPackages) {
            Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation(repositoryPackage, Repository.class);
            List<Class<?>> collect = classes.stream()
                    .filter(clazz -> {


                    } )
                    .collect(Collectors.toList());
            classSet.addAll(collect);
        }
        classSet.stream().map(clazz->{

        })
        HibernateUtil.init(properties, classSet.toArray(new Class[0]));
        for (Class<?> aClass : classSet) {
            Object proxy = ProxyUtil.newProxyInstance(new JpaInvocationHandler(aClass), aClass);
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
            beanDefinition.setBean(proxy);
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
            beanDefinition.setBeanClass(aClass);
            applicationContext.addBean(beanDefinition);
        }
    }

    @Override
    public void beforeInvoke() {

    }

    @Override
    public void invokeBeanFactoryPostProcess() {

    }

    @Override
    public void initBeanPostProcess() {

    }

    @Override
    public void loadListener() {

    }

    @Override
    public void initAllBeans() {

    }

    @Override
    public void finish() {

    }

    @Override
    public String beanFactoryId() {
        return null;
    }

    @Override
    public Class getAppRunClass() {
        return null;
    }

    @Override
    public Map<Class<?>, BeanDefinition> getSingletonBeanDefinitionMap() {
        return null;
    }

    @Override
    public BeanDefinition getBeanDefinition(Class<?> clazz) {
        return null;
    }

    @Override
    public <T> T getProperty(String propertyName) {
        return null;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public Collection<BeanDefinition> getBeanDefinitions(Class<?> clazz) {
        return null;
    }

    @Override
    public void addBean(Class<?> clazz) {

    }

    @Override
    public void addBean(BeanDefinition beanDefinition) {

    }
}

