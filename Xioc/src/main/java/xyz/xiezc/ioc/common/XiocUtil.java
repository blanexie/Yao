package xyz.xiezc.ioc.common;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.definition.BeanStatusEnum;
import xyz.xiezc.ioc.definition.BeanTypeEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class XiocUtil {


    /**
     * 注入依赖， 生成bean的方法
     *
     * @param beanDefinition
     * @param contextUtil
     * @return
     */
    public static Object createBean(BeanDefinition beanDefinition, ContextUtil contextUtil) {
        BeanStatusEnum beanStatu = beanDefinition.getBeanStatus();
        if (beanStatu == BeanStatusEnum.HalfCooked || beanStatu == BeanStatusEnum.Completed) {
            //已经初始化完成了
            return beanDefinition.getBean();
        }
        Method method = beanDefinition.getMethod();
        Class<?> beanClass;
        Object bean;
        //判断是否是bean方法
        if (method != null) {
            BeanDefinition parentBeanDefinition = beanDefinition.getParentBeanDefinition();
            Object parentBean = createBean(parentBeanDefinition, contextUtil);
            int parameterCount = method.getParameterCount();
            if (parameterCount != 0) {
                throw new RuntimeException("目前@Bean注解的方法不支持带有参数，method:" + method.getName());
            }
            beanClass = method.getReturnType();
            bean = ReflectUtil.invoke(parentBean, method);
        } else {
            //实例化对象
            beanClass = beanDefinition.getBeanClass();
            bean = ReflectUtil.newInstanceIfPossible(beanClass);
        }

        beanDefinition.setBean(bean);
        beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        //检查注入字段 是否完整
        List<BeanSignature> injectBeans = beanDefinition.getInjectBeans();
        if (injectBeans != null) {
            //校验对应的所有要注入的bean
            checkInjectBeans(beanClass, injectBeans, contextUtil);
            //到这里说明所有的依赖都能正常找到， 开始注入
            Field[] fields = ReflectUtil.getFields(beanClass);
            for (Field field : fields) {
                dealInjectField(bean, injectBeans, field, contextUtil);
            }
        }
        //bean 所有对象设置完整
        beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        return bean;
    }

    /**
     * 处理部分 需要注入字段方法
     *
     * @param bean
     * @param injectBeans
     * @param field
     */
    private static void dealInjectField(Object bean, List<BeanSignature> injectBeans, Field field, ContextUtil contextUtil) {
        //获取字段的类型和对应的beanSingature
        Class<?> type = field.getType();
        Optional<BeanSignature> first = injectBeans.stream()
                .filter(injectBean -> injectBean.getBeanClass() == type)
                .findFirst();
        BeanSignature beanSignature = first.orElse(null);
        if (beanSignature == null) {
            return;
        }

        Inject inject = field.getAnnotation(Inject.class);
        if (inject != null) {
            if (beanSignature.getBeanTypeEnum() != BeanTypeEnum.bean) {
                throw new RuntimeException("bean类型的字段无法注入：" + beanSignature.toString());
            }
            //获取对应的bean
            BeanDefinition beanDefinition = contextUtil.getBeanDefinitionBySignature(beanSignature);
            if (beanDefinition == null) {
                throw new RuntimeException("容器中无法找到对应的注入bean：" + beanSignature.toString());
            }
            Object fieldValue = createBean(beanDefinition, contextUtil);
            //注入数组, 集合无法注入。 单例模式注入也没用
            if (type.isArray() || type.isAssignableFrom(Collection.class)) {
                throw new RuntimeException(" 数组类型的字段无法注入：" + beanSignature.toString());
            }
            //设置字段值
            ReflectUtil.setFieldValue(bean, field, fieldValue);
        }

        //配置注入
        Value value = field.getAnnotation(Value.class);
        if (value != null) {
            if (beanSignature.getBeanTypeEnum() != BeanTypeEnum.properties) {
                throw new RuntimeException("配置类型的字段无法注入：" + beanSignature.toString());
            }
            //注入配置字段
            String s = contextUtil.getSetting().get(beanSignature.getBeanName());
            Object convert = Convert.convert(type, s);
            ReflectUtil.setFieldValue(bean, field, convert);
        }
    }

    /**
     * 检验bean的要注入的字段
     *
     * @param beanClass
     * @param injectBeans
     */
    private static void checkInjectBeans(Class<?> beanClass, List<BeanSignature> injectBeans, ContextUtil contextUtil) {
        for (BeanSignature injectBean : injectBeans) {
            if (injectBean.getBeanTypeEnum() == BeanTypeEnum.bean) {
                BeanDefinition injectBeanDefinition = contextUtil.getBeanDefinitionBySignature(injectBean);
                if (injectBeanDefinition == null) {
                    throw new RuntimeException(beanClass.getTypeName() + " 这个bean找不到对应的注入对象：" + injectBeanDefinition.toString());
                }
            }
            if (injectBean.getBeanTypeEnum() == BeanTypeEnum.properties) {
                Class<?> injectBeanBeanClass = injectBean.getBeanClass();
                if (!ClassUtil.isBasicType(injectBeanBeanClass) && String.class != injectBeanBeanClass) {
                    throw new RuntimeException(beanClass.getTypeName() + " 这个bean注入value配置的字段：" + injectBean.toString() + "应该注入基本类型");
                }
            }
        }
    }


    public static BeanDefinition dealBeanAnnotation(Annotation annotation, Class clazz, ContextUtil contextUtil) {
        BeanSignature beanSignature = new BeanSignature();
        beanSignature.setBeanClass(clazz);
        String beanName = AnnotationUtil.getAnnotationValue(clazz, annotation.annotationType(), "value");
        if (StrUtil.isBlank(beanName)) {
            beanName = clazz.getTypeName();
        }
        beanSignature.setBeanName(beanName);

        BeanDefinition beanDefinition = contextUtil.getBeanDefinitionBySignature(beanSignature);
        //如果容器中不存在 这个bean。 就要放入
        if (beanDefinition == null) {
            beanDefinition = new BeanDefinition();
        }
        //覆盖更新这个信息
        //设置bean的基本签名
        beanDefinition.setBeanSignature(beanSignature);
        //获取bean的inject 注入bean的信息
        Field[] fields = ReflectUtil.getFields(beanSignature.getBeanClass());
        //获取这个bean的所有待注入的信息
        for (Field field : fields) {
            BeanSignature beanSignature1 = dealInjectFieldBean(field, beanSignature.getBeanClass());
            List<BeanSignature> injectBeans = beanDefinition.getInjectBeans();
            if (injectBeans == null) {
                injectBeans = CollUtil.newArrayList();
                beanDefinition.setInjectBeans(injectBeans);
            }
            injectBeans.add(beanSignature1);
        }

        beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        return beanDefinition;
    }


    private static BeanSignature dealInjectFieldBean(Field field, Class<?> clazz) {
        Inject inject = AnnotationUtil.getAnnotation(field, Inject.class);
        if (inject != null) {
            String injectBeanName = inject.value();
            if (StrUtil.isBlank(injectBeanName)) {
                injectBeanName = field.getName();
            }
            BeanSignature beanSignature = createBeanSingature(field, injectBeanName);
            beanSignature.setBeanTypeEnum(BeanTypeEnum.bean);
            return beanSignature;
        }

        Value value = AnnotationUtil.getAnnotation(field, Value.class);
        String injectBeanName = value.value();
        if (StrUtil.isBlank(injectBeanName)) {
            injectBeanName = clazz.getTypeName() + "#" + field.getName();
        }
        BeanSignature beanSignature = createBeanSingature(field, injectBeanName);
        beanSignature.setBeanTypeEnum(BeanTypeEnum.properties);
        return beanSignature;

    }

    private static BeanSignature createBeanSingature(Field field, String injectBeanName) {
        BeanSignature beanSignature = new BeanSignature();
        Class<?> type = field.getType();
        beanSignature.setBeanClass(type);
        beanSignature.setBeanName(injectBeanName);
        return beanSignature;
    }

}
