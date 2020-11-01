package xyz.xiezc.ioc.starter.core;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Init;
import xyz.xiezc.ioc.starter.annotation.core.Value;
import xyz.xiezc.ioc.starter.common.asm.AsmUtil;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.core.context.BeanFactory;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.core.definition.ParamDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanPostProcess;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 4:00 下午
 **/
@Slf4j
public final class BeanCreateUtil {

    /**
     * 用来装载各个BeanPostProcess的实体类的， 用于创建bean的时候
     */
    PriorityQueue<BeanPostProcess> beanPostProcessPriorityQueue = new PriorityQueue<>();

    private volatile static BeanCreateUtil beanCreateUtil;
    /**
     * 正在创建中的bean，用来解决循环依赖的问题
     */
    Set<Class> buildingSet = new HashSet<>();


    /**
     * 容器对象
     */
    @Getter
    private final BeanFactory beanFactory;



    public void clear(){
        buildingSet.clear();
        beanPostProcessPriorityQueue.clear();
    }
    /**
     * 单例模式
     *
     * @param beanFactory
     * @return
     */
    public static BeanCreateUtil getInstacne(BeanFactory beanFactory) {
        if (beanCreateUtil == null) {
            synchronized (BeanCreateUtil.class) {
                if (beanCreateUtil == null) {
                    beanCreateUtil = new BeanCreateUtil(beanFactory);
                }
            }
        } else {
            BeanFactory beanFactory1 = beanCreateUtil.getBeanFactory();
            if (Objects.equals(beanFactory1.beanFactoryId(), beanFactory.beanFactoryId())) {
                return beanCreateUtil;
            } else {
                throw new RuntimeException("一个应用程序中目前只支持一个容器");
            }
        }
        return beanCreateUtil;
    }


    private BeanCreateUtil(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    /**
     * 创建并初始化bean
     */
    public void createAndInitBeanDefinition(BeanDefinition beanDefinition) {
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
            if (!buildingSet.add(beanDefinition.getBeanClass())) {
                throw new RuntimeException("class:{} 被循环依赖了");
            }

            for (BeanPostProcess beanPostProcess : beanPostProcessPriorityQueue) {
                if (!beanPostProcess.beforeInstance(beanFactory, beanDefinition)) {
                    break;
                }
            }
            createBean(beanDefinition, this.getBeanFactory());
        }

        if (beanDefinition.getBeanStatus() == BeanStatusEnum.HalfCooked) {
            for (BeanPostProcess beanPostProcess : beanPostProcessPriorityQueue) {
                if (!beanPostProcess.beforeInject(beanFactory, beanDefinition)) {
                    break;
                }
            }
            injectBean(beanDefinition, this.getBeanFactory());
        }

        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Injected) {
            for (BeanPostProcess beanPostProcess : beanPostProcessPriorityQueue) {
                if (!beanPostProcess.beforeInit(beanFactory, beanDefinition)) {
                    break;
                }
            }
            initBean(beanDefinition, this.getBeanFactory());
        }

        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Injected) {
            for (BeanPostProcess beanPostProcess : beanPostProcessPriorityQueue) {
                if (!beanPostProcess.afterInit(beanFactory, beanDefinition)) {
                    break;
                }
            }
            beanDefinition.setBeanStatus(BeanStatusEnum.Inited);
        }

        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Inited) {
            log.info("{} 初始化完成", beanDefinition.getBeanClass().getName());
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
            Class<?> beanClass = beanDefinition.getBeanClass();
            if (ClassUtil.isAssignable(BeanPostProcess.class, beanClass)) {
                beanPostProcessPriorityQueue.offer(beanDefinition.getBean());
            }
        }
    }


    private void initBean(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object bean = beanDefinition.getBean();
        Method[] publicMethods = ClassUtil.getPublicMethods(beanClass);
        for (Method publicMethod : publicMethods) {
            Init initAnnotation = AnnotationUtil.getAnnotation(publicMethod, Init.class);
            int parameterCount = publicMethod.getParameterCount();
            if (initAnnotation != null && parameterCount > 0) {
                throw new RuntimeException("@Init 注解的方法必须是无参的， beanClass:" + beanClass.getName() + " ; method: " + publicMethod.getName());
            }

            if (initAnnotation != null) {
                //主动调用初始化方法
                ReflectUtil.invoke(bean, publicMethod);
            }
        }

        beanDefinition.setBeanStatus(BeanStatusEnum.Inited);
    }

    private void injectBean(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object bean = beanDefinition.getBean();
        Field[] fields = ReflectUtil.getFields(beanClass);
        for (Field field : fields) {
            Value valueAnnotation = AnnotationUtil.getAnnotation(field, Value.class);
            if (valueAnnotation != null) {
                String name = valueAnnotation.value();
                String property = beanFactory.getProperty(name);
                //进行基本的类型转换；
                Object covert = this.covert(property, field.getType());
                ReflectUtil.setFieldValue(bean, field, covert);
            }
            Autowire annotation = AnnotationUtil.getAnnotation(field, Autowire.class);
            if (annotation != null) {
                Object value = beanFactory.getBean(field);
                ReflectUtil.setFieldValue(bean, field, value);
            }
        }
        beanDefinition.setBeanStatus(BeanStatusEnum.Injected);
    }

    private <T> T covert(String value, Class<T> clazz) {
        try {
            return Convert.convert(clazz, value);
        } catch (ConvertException e) {
            throw e;
        }
    }


    /**
     * 创建bean
     *
     * @param beanDefinition
     * @param beanFactory
     */
    private void createBean(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.bean) {
            Object bean = ReflectUtil.newInstanceIfPossible(beanDefinition.getBeanClass());
            beanDefinition.setBean(bean);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }

        if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.methodBean) {
            MethodDefinition invokeMethodBean = beanDefinition.getInvokeMethodBean();
            this.createBean(invokeMethodBean.getBeanDefinition(), beanFactory);
            Object bean = invokeMethodBean.getBeanDefinition().getBean();
            Method method = invokeMethodBean.getMethod();
            ParamDefinition[] methodParamsAndAnnotaton = AsmUtil.getMethodParamsAndAnnotaton(method);

            for (ParamDefinition paramDefinition : methodParamsAndAnnotaton) {
                //获取需要注入的参数
                Object beans = beanFactory.getBean(paramDefinition.getParameter());
                paramDefinition.setParam(beans);
            }

            Object[] params = new Object[methodParamsAndAnnotaton.length];
            for (int i = 0; i < methodParamsAndAnnotaton.length; i++) {
                params[i] = methodParamsAndAnnotaton[i].getParam();
            }

            Object invoke = ReflectUtil.invoke(bean, method, params);
            beanDefinition.setBean(invoke);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }
    }


}