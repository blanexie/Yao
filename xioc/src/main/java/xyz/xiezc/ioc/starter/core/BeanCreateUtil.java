package xyz.xiezc.ioc.starter.core;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.Log;
import lombok.Getter;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Init;
import xyz.xiezc.ioc.starter.annotation.core.Value;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.core.context.BeanFactory;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanPostProcess;
import xyz.xiezc.ioc.starter.exception.CircularDependenceException;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 单例的模式构建对象
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 4:00 下午
 **/
public final class BeanCreateUtil {

    Log log = Log.get(BeanCreateUtil.class);
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


    public void clear() {
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
    public void createAndInject(BeanDefinition beanDefinition) {
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Completed) {
            return;
        }

        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
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
    }


    public void initAndComplete(BeanDefinition beanDefinition){
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Injected) {
            for (BeanPostProcess beanPostProcess : beanPostProcessPriorityQueue) {
                if (!beanPostProcess.beforeInit(beanFactory, beanDefinition)) {
                    break;
                }
            }
            initBean(beanDefinition, this.getBeanFactory());
        }

        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Inited) {
            for (BeanPostProcess beanPostProcess : beanPostProcessPriorityQueue) {
                if (!beanPostProcess.afterInit(beanFactory, beanDefinition)) {
                    break;
                }
            }
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
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
                //获取要注入的bean的部分类型信息
                ParamType realType = getRealType(field);
                //检查是否有循环依赖的问题
                if (realType.paramTypeEnum == ParamTypeEnum.isArray) {
                    Collection<Object> injectBeans = getInjectBeans(realType.baseType);
                    Object[] obj = injectBeans.toArray();
                    ReflectUtil.setFieldValue(bean, field, obj);
                }
                if (realType.paramTypeEnum == ParamTypeEnum.isCollection) {
                    Collection<Object> injectBeans = getInjectBeans(realType.baseType);
                    ReflectUtil.setFieldValue(bean, field, injectBeans);
                }
                if (realType.paramTypeEnum == ParamTypeEnum.base) {
                    BeanDefinition beanDefinition1 = beanFactory.getBeanDefinition(realType.baseType);
                    if(beanDefinition1.getBean()==null){
                        createAndInject(beanDefinition1);
                    }
                    ReflectUtil.setFieldValue(bean, field, beanDefinition1.getBean());
                }
            }
        }
        beanDefinition.setBeanStatus(BeanStatusEnum.Injected);
    }

    private Collection<Object> getInjectBeans(Class baseType) {
        Collection<BeanDefinition> beanDefinitions = beanFactory.getBeanDefinitions(baseType);
        for (BeanDefinition definition : beanDefinitions) {
            createAndInject(definition);
        }
        Collection<Object> collect = beanDefinitions.stream()
                .map(definition -> {
                    if(definition.getBean()==null){
                        createAndInject(definition);
                    }
                    return definition.getBean();
                }).collect(Collectors.toList());
        return collect;
    }

    private boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    private boolean isCollection(Class<?> clazz) {
        return ClassUtil.isAssignable(Collection.class, clazz);
    }


    private ParamType getRealType(Field field) {
        ParamType paramType = new ParamType();
        Class<?> type = field.getType();
        if (isArray(type)) {
            Class<?> componentType = type.getComponentType();
            paramType.baseType = componentType;
            paramType.paramTypeEnum = ParamTypeEnum.isArray;
            return paramType;
        } else if (isCollection(type)) {
            //是集合
            Type genericType = field.getGenericType();
            if (genericType != null && genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                //得到泛型里的class类型对象
                Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];
                paramType.baseType = genericClazz;
                paramType.paramTypeEnum = ParamTypeEnum.isCollection;
                return paramType;
            } else {
                throw new RuntimeException("@Autowire注解的字段为Collection类型时，需要写明泛型类型才能注入");
            }
        } else {
            paramType.baseType = type;
            paramType.paramTypeEnum = ParamTypeEnum.base;
            return paramType;
        }
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

    }

}

/**
 *
 */
class ParamType {


    /**
     * 真实类型
     */
    Class<?> baseType;

    /**
     *
     */
    ParamTypeEnum paramTypeEnum;

}

enum ParamTypeEnum {

    base, isArray, isCollection;
}