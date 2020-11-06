package xyz.xiezc.ioc.starter.core;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
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

    /**
     * 用来装载各个BeanPostProcess的实体类的， 用于创建bean的时候
     */
    PriorityQueue<BeanPostProcess> beanPostProcessPriorityQueue = new PriorityQueue<>();

    private volatile static BeanCreateUtil beanCreateUtil;
    /**
     * 正在创建中的bean，用来解决循环依赖的问题
     */
    Map<Class, BeanDefinition> buildBeanDefinitionMap = new HashMap<>();


    /**
     * 容器对象
     */
    @Getter
    private final BeanFactory beanFactory;


    public void clear() {
        buildBeanDefinitionMap.clear();
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
            for (BeanPostProcess beanPostProcess : beanPostProcessPriorityQueue) {
                if (!beanPostProcess.beforeInstance(beanFactory, beanDefinition)) {
                    break;
                }
            }
            createBean(beanDefinition, this.getBeanFactory());
            //放入创建中
            buildBeanDefinitionMap.put(beanDefinition.getBeanClass(), beanDefinition);
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
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
            Class<?> beanClass = beanDefinition.getBeanClass();
            if (ClassUtil.isAssignable(BeanPostProcess.class, beanClass)) {
                beanPostProcessPriorityQueue.offer(beanDefinition.getCompletedBean());
            }
            buildBeanDefinitionMap.remove(beanDefinition.getBeanClass());
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
                ParamType realType = getRealType(field);
                if (realType.paramTypeEnum == ParamTypeEnum.isArray) {
                    List<Object> collect = getBeans(beanFactory, realType.baseType);
                    Object[] obj = collect.toArray();
                    ReflectUtil.setFieldValue(bean, field, obj);
                }
                if (realType.paramTypeEnum == ParamTypeEnum.isCollection) {
                    List<Object> beans = getBeans(beanFactory, realType.baseType);
                    ReflectUtil.setFieldValue(bean, field, beans);
                }
                if (realType.paramTypeEnum == ParamTypeEnum.base) {
                    BeanDefinition value = beanFactory.getBeanDefinition(realType.baseType);
                    if (value.getBeanStatus() == BeanStatusEnum.Original) {
                        this.createAndInitBeanDefinition(value);
                    }
                    ReflectUtil.setFieldValue(bean, field, value.getBean());
                }
            }
        }
        beanDefinition.setBeanStatus(BeanStatusEnum.Injected);
    }

    private boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    private boolean isCollection(Class<?> clazz) {
        return ClassUtil.isAssignable(Collection.class, clazz);
    }


    private ParamType getRealType(Field field) {
        Class<?> type = field.getType();
        if (isArray(type)) {
            Class<?> componentType = type.getComponentType();
            ParamType paramType = new ParamType();
            paramType.baseType = componentType;
            paramType.paramTypeEnum = ParamTypeEnum.isArray;
            return paramType;
        } else if (isCollection(type)) {
            //是集合
            Type genericType = field.getGenericType();
            return getParamType(genericType);
        } else {
            ParamType paramType = new ParamType();
            paramType.baseType = type;
            paramType.paramTypeEnum = ParamTypeEnum.base;
            return paramType;
        }
    }

    private ParamType getRealType(Parameter parameter) {
        ParamType paramType = new ParamType();
        Class<?> type = parameter.getType();
        if (isArray(type)) {
            Class<?> componentType = type.getComponentType();
            paramType.baseType = componentType;
            paramType.paramTypeEnum = ParamTypeEnum.isArray;
            return paramType;
        } else if (isCollection(type)) {
            //是集合
            Type genericType = parameter.getParameterizedType();
            return getParamType( genericType);
        } else {
            paramType.baseType = type;
            paramType.paramTypeEnum = ParamTypeEnum.base;
            return paramType;
        }
    }


    /**
     * 这个方法用于真实的类型的获取， 不要传入数组或者集合类型
     *
     * @param genericType
     * @return
     */
    private ParamType getParamType(Type genericType) {
        ParamType paramType = new ParamType();
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

        //方法类型的bean在创建过程中会有依赖， 需要慎重考虑循环依赖的问题
        if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.methodBean) {
            //检查自己是否已经在创建中
            if (buildBeanDefinitionMap.containsKey(beanDefinition.getBeanClass())) {
                throw new CircularDependenceException("@Bean注解的方法出现了循环依赖的问题," + beanDefinition);
            }
            MethodDefinition invokeMethodBean = beanDefinition.getInvokeMethodBean();
            this.createBean(invokeMethodBean.getBeanDefinition(), beanFactory);
            Object bean = invokeMethodBean.getBeanDefinition().getBean();
            Method method = invokeMethodBean.getMethod();

            //获取所有的依赖信息
            ParamDefinition[] methodParamsAndAnnotaton = AsmUtil.getMethodParamsAndAnnotaton(method);
            for (ParamDefinition paramDefinition : methodParamsAndAnnotaton) {
                //获取需要注入的参数
                ParamType realType = getRealType(paramDefinition.getParameter());
                if (realType.paramTypeEnum == ParamTypeEnum.isArray) {
                    List<Object> collect = getBeans(beanFactory, realType.baseType);
                    Object[] obj = collect.toArray();
                    paramDefinition.setParam(obj);
                }
                if (realType.paramTypeEnum == ParamTypeEnum.isCollection) {
                    List<Object> collect = getBeans(beanFactory, realType.baseType);
                    paramDefinition.setParam(collect);
                }
                if (realType.paramTypeEnum == ParamTypeEnum.base) {
                    BeanDefinition definition = beanFactory.getBeanDefinition(realType.baseType);
                    if (definition.getBeanStatus() == BeanStatusEnum.Original) {
                        this.createAndInitBeanDefinition(definition);
                    }
                    paramDefinition.setParam(definition.getBean());
                }
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

    @NotNull
    private List<Object> getBeans(BeanFactory beanFactory, Class baseType) {
        Collection<BeanDefinition> beans = beanFactory.getBeanDefinitions(baseType);
        //如果依赖的对象还没有创建成功，则会报错
        return beans.stream()
                .map(definition -> {
                    if (definition.getBeanStatus() == BeanStatusEnum.Original) {
                        this.createAndInitBeanDefinition(definition);
                    }
                    return definition.getBean();
                }).collect(Collectors.toList());
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