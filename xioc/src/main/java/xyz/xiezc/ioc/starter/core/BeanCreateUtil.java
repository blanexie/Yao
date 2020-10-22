package xyz.xiezc.ioc.starter.core;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Value;
import xyz.xiezc.ioc.starter.common.asm.AsmUtil;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.core.definition.ParamDefinition;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 4:00 下午
 **/
public final class BeanCreateUtil {


    private final static BeanCreateUtil beanCreateUtil = new BeanCreateUtil();

    public static BeanCreateUtil getInstacne() {
        return beanCreateUtil;
    }

    private BeanCreateUtil() {

    }

    public BeanDefinition createAndInjectBean(BeanDefinition beanDefinition, ApplicationContext applicationContext) {
        //第一步实例化对象
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Original) {
            createBean(beanDefinition, applicationContext);
        }
        //第二步注入对象
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.HalfCooked) {
            injectBean(beanDefinition, applicationContext);
        }
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.injectField) {
            return beanDefinition;
        }
        throw new RuntimeException("实例化和依赖注入类型失败");
    }

    private void injectBean(BeanDefinition beanDefinition, ApplicationContext applicationContext) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object bean = beanDefinition.getBean();
        Field[] fields = ReflectUtil.getFields(beanClass);
        for (Field field : fields) {
            Value valueAnnotation = AnnotationUtil.getAnnotation(field, Value.class);
            if (valueAnnotation != null) {
                String name = valueAnnotation.value();
                String property = applicationContext.getProperty(name);
                //进行基本的类型转换；
                Object covert = this.covert(property, field.getType());
                ReflectUtil.setFieldValue(bean, field, covert);
            }
            Autowire annotation = AnnotationUtil.getAnnotation(field, Autowire.class);
            if (annotation != null) {
                Object value = this.getBeans(field, applicationContext);
                ReflectUtil.setFieldValue(bean, field, value);
            }
        }
    }


    public <T> T covert(String value, Class<T> clazz) {
        try {
            return Convert.convert(clazz, value);
        } catch (ConvertException e) {
            throw e;
        }
    }


    public boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    public boolean isCollection(Class<?> clazz) {
        return ClassUtil.isAssignable(Collection.class, clazz);
    }


    /**
     * 创建bean
     *
     * @param beanDefinition
     * @param applicationContext
     */
    private void createBean(BeanDefinition beanDefinition, ApplicationContext applicationContext) {
        if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.bean) {
            Object bean = ReflectUtil.newInstanceIfPossible(beanDefinition.getBeanClass());
            beanDefinition.setBean(bean);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }

        if (beanDefinition.getBeanTypeEnum() == BeanTypeEnum.methodBean) {
            MethodDefinition invokeMethodBean = beanDefinition.getInvokeMethodBean();
            this.createAndInjectBean(invokeMethodBean.getBeanDefinition(), applicationContext);
            Object bean = invokeMethodBean.getBeanDefinition().getBean();
            Method method = invokeMethodBean.getMethod();
            ParamDefinition[] methodParamsAndAnnotaton = AsmUtil.getMethodParamsAndAnnotaton(method);

            for (ParamDefinition paramDefinition : methodParamsAndAnnotaton) {
                //获取需要注入的参数
                Object beans = this.getBeans(paramDefinition.getParameter(), applicationContext);
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

    public Object getBeans(Field field, ApplicationContext applicationContext) {
        Class<?> type = field.getType();
        if (isArray(type)) {
            //是数组
            Class<?> componentType = type.getComponentType();
            List<Object> params = applicationContext.getBeans(componentType);
            Object[] obj = new Object[params.size()];
            for (int i = 0; i < params.size(); i++) {
                obj[i] = params.get(i);
            }
            return params;
        } else if (isCollection(type)) {
            //是集合
            Type genericType = field.getGenericType();
            if (genericType != null && genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                //得到泛型里的class类型对象
                Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];
                List<Object> params = applicationContext.getBeans(genericClazz);
                return params;
            } else {
                throw new RuntimeException("@Autowire注解的字段为Collection类型时，需要写明泛型类型才能注入");
            }
        } else {
            //是常规类型
            Object value = applicationContext.getBean(type);
            return value;
        }
    }

    public Object getBeans(Parameter parameter, ApplicationContext applicationContext) {
        Class<?> type = parameter.getType();
        if (isArray(type)) {
            //是数组
            Class<?> componentType = type.getComponentType();
            List<Object> params = applicationContext.getBeans(componentType);
            Object[] obj = new Object[params.size()];
            for (int i = 0; i < params.size(); i++) {
                obj[i] = params.get(i);
            }
            return params;
        } else if (isCollection(type)) {
            //是集合
            Type genericType = parameter.getParameterizedType();
            if (genericType != null && genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                //得到泛型里的class类型对象
                Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];
                List<Object> params = applicationContext.getBeans(genericClazz);
                return params;
            } else {
                throw new RuntimeException("@Autowire注解的字段为Collection类型时，需要写明泛型类型才能注入");
            }
        } else {
            //是常规类型
            Object value = applicationContext.getBean(type);
            return value;
        }
    }
}