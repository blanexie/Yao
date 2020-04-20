package xyz.xiezc.ioc.common;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.annotation.CombinationAnnotationElement;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;
import xyz.xiezc.ioc.asm.AsmUtil;
import xyz.xiezc.ioc.definition.*;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanScopeEnum;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

        //检查所有需要注入字段的依赖是否都存在
        List<FieldDefinition> annotationFiledDefinitions = beanDefinition.getAnnotationFiledDefinitions();
        checkFieldDefinitions(contextUtil, annotationFiledDefinitions);
        //检查methodBean的invoke方法的参数依赖是否存在
        MethodDefinition methodBeanInvoke = beanDefinition.getMethodBeanInvoke();
        checkMethodDefinition(contextUtil, methodBeanInvoke);
        //检查 bean的init方法的参数依赖是否存在
        checkMethodDefinition(contextUtil, beanDefinition.getInitMethodDefinition());

        //初始化对应bean
        doCreateBean(beanDefinition, contextUtil);

        //调用对应bean的init方法
        MethodDefinition initMethodDefinition = beanDefinition.getInitMethodDefinition();
        if (initMethodDefinition != null) {
            Object bean = beanDefinition.getBean();
            ReflectUtil.invoke(bean, initMethodDefinition.getMethodName());
        }

        //bean 所有对象设置完整
        beanDefinition.setBeanStatus(BeanStatusEnum.Completed);


        return beanDefinition.getBean();
    }

    private static void doCreateBean(BeanDefinition beanDefinition, ContextUtil contextUtil) {
        List<FieldDefinition> annotationFiledDefinitions = beanDefinition.getAnnotationFiledDefinitions();
        //实例化普通bean
        if (beanDefinition.getBeanScopeEnum() == BeanScopeEnum.bean) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
            beanDefinition.setBean(bean);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }
        //实例化 methodBean
        if (beanDefinition.getBeanScopeEnum() == BeanScopeEnum.methodBean) {
            MethodDefinition methodBeanInvoke = beanDefinition.getMethodBeanInvoke();
            BeanDefinition beanDefinitionParent = methodBeanInvoke.getBeanDefinition();
            Object parent;
            BeanStatusEnum beanStatus = beanDefinitionParent.getBeanStatus();
            if (beanStatus == BeanStatusEnum.Completed) {
                parent = beanDefinitionParent.getBean();
            } else {
                parent = createBean(beanDefinitionParent, contextUtil);
            }

            ParamDefinition[] paramDefinitions = methodBeanInvoke.getParamDefinitions();
            for (ParamDefinition paramDefinition : paramDefinitions) {
                Object param = paramDefinition.getParam();
                if (param != null) {
                    continue;
                }
                BeanDefinition beanDefinitionParam = contextUtil.getBeanDefinition(paramDefinition.getBeanName(), paramDefinition.getParamType());
                beanStatus = beanDefinitionParent.getBeanStatus();
                if (beanStatus == BeanStatusEnum.Completed) {
                    param = beanDefinitionParam.getBean();
                } else {
                    param = createBean(beanDefinitionParam, contextUtil);
                }
                paramDefinition.setParam(param);
            }
            List<Object> collect = CollUtil.newArrayList(paramDefinitions).stream()
                    .map(ParamDefinition::getParam)
                    .collect(Collectors.toList());
            Object[] objects = collect.toArray();
            Object invoke = ReflectUtil.invoke(parent, methodBeanInvoke.getMethodName(), objects);
            beanDefinition.setBean(invoke);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }
        //需要特殊处理
        if (beanDefinition.getBeanScopeEnum() == BeanScopeEnum.factoryBean) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Object bean = ReflectUtil.newInstanceIfPossible(beanClass);
            beanDefinition.setBean(bean);
            beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
        }

        //设置字段的属性值
        for (FieldDefinition fieldDefinition : annotationFiledDefinitions) {
            Object obj = fieldDefinition.getObj();
            if (obj instanceof BeanDefinition) {
                obj = createBean((BeanDefinition) obj, contextUtil);
            }
            ReflectUtil.setFieldValue(beanDefinition.getBean(), fieldDefinition.getFieldName(), obj);
        }
        beanDefinition.setBeanStatus(BeanStatusEnum.injectField);
    }


    /**
     * 检查字段的依赖是否存在
     *
     * @param contextUtil
     * @param annotationFiledDefinitions
     */
    private static void checkFieldDefinitions(ContextUtil contextUtil, List<FieldDefinition> annotationFiledDefinitions) {
        for (FieldDefinition annotationFiledDefinition : annotationFiledDefinitions) {
            if (annotationFiledDefinition.getObj() != null) {
                continue;
            }
            /**
             *  annotationFiledDefinitions 中保存的是bean中的所有包含注解的字段，
             *  但这些注解不一定是Inject和Value注解，可能还有一些自定义注解。
             *  所以如果beanName字段有值，就说明需要中容器中取得对应的bean来放入FieldDefinition的bean字段中。
             *  Value注解是从配置中取值放入FieldDefinition的bean字段中。
             *  后面初始化的时候会使用FieldDefinition的bean字段
             **/
            if (StrUtil.isBlank(annotationFiledDefinition.getBeanName())) {
                continue;
            }

            String beanName = annotationFiledDefinition.getBeanName();
            Class<?> fieldType = annotationFiledDefinition.getFieldType();
            BeanDefinition beanDefinition = contextUtil.getBeanDefinition(beanName, fieldType);
            if (beanDefinition == null) {
                ExceptionUtil.wrapAndThrow(new RuntimeException("未找到注入的字段；" + annotationFiledDefinition));
                break;
            } else {
                annotationFiledDefinition.setObj(beanDefinition);
            }
        }
    }

    /**
     * 检查方法的注入依赖是否都存在
     *
     * @param contextUtil
     * @param methodBeanInvoke
     */
    private static void checkMethodDefinition(ContextUtil contextUtil, MethodDefinition methodBeanInvoke) {
        ParamDefinition[] paramDefinitions = methodBeanInvoke.getParamDefinitions();
        for (ParamDefinition paramDefinition : paramDefinitions) {
            Object param = paramDefinition.getParam();
            if (param != null) {
                continue;
            }
            String beanName = paramDefinition.getBeanName();
            Class paramType = paramDefinition.getParamType();
            BeanDefinition beanDefinition = contextUtil.getBeanDefinition(beanName, paramType);
            if (beanDefinition == null) {
                ExceptionUtil.wrapAndThrow(new RuntimeException("未找到方法的注入参数：" + paramDefinition));
                break;
            } else {
                paramDefinition.setParam(beanDefinition);
            }
        }
    }

    public static BeanDefinition dealBeanAnnotation(Annotation annotation, Class clazz, ContextUtil contextUtil) {

        Class<?> beanClass = clazz;
        String beanName = AnnotationUtil.getAnnotationValue(clazz, annotation.annotationType(), "value");
        if (StrUtil.isBlank(beanName)) {
            beanName = clazz.getTypeName();
        }

        //获取容器中是否存在这个bean
        BeanDefinition beanDefinition = contextUtil.getBeanDefinition(beanName, beanClass);
        //如果容器中不存在 这个bean。 就要放入
        if (beanDefinition == null) {
            beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClass(beanClass);
            beanDefinition.setBeanName(beanName);
        }

        //获取这个容器中的注解
        AnnotatedElement annotatedElement = new CombinationAnnotationElement(beanClass);
        beanDefinition.setAnnotatedElement(annotatedElement);
        if (ClassUtil.isAssignable(FactoryBean.class, beanClass)) {
            beanDefinition.setBeanScopeEnum(BeanScopeEnum.factoryBean);
            //对应的beanClass需要重新设置
            Type[] genericInterfaces = beanClass.getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                String typeName = genericInterface.getTypeName();
                //TODO  需要调试
                //如果得到的泛型类型是 ParameterizedType（参数化类型）类型的话
                if (ClassUtil.isAssignable(ParameterizedType.class, genericInterface.getClass())
                        || StrUtil.equals(typeName, "FactoryBean")
                ) {
                    //将类型转换为 参数类型类
                    ParameterizedType ptype = (ParameterizedType) genericInterface;
                    //得到  泛型类型的实际  类型
                    Type[] iType = ptype.getActualTypeArguments();
                    beanDefinition.setBeanClass((Class<?>) iType[0]);
                }
            }
        } else {
            beanDefinition.setBeanScopeEnum(BeanScopeEnum.bean);
        }

        //获取bean的inject 注入bean的信息
        Field[] fields = ReflectUtil.getFields(beanClass);
        //获取这个bean的所有待注入的信息
        for (Field field : fields) {
            FieldDefinition fieldDefinition = dealFieldDefinition(field, beanDefinition);
            if (fieldDefinition == null) {
                continue;
            }
            List<FieldDefinition> injectFiledDefinitions = beanDefinition.getAnnotationFiledDefinitions();
            if (injectFiledDefinitions == null) {
                injectFiledDefinitions = CollUtil.newArrayList();
                beanDefinition.setAnnotationFiledDefinitions(injectFiledDefinitions);
            }
            injectFiledDefinitions.add(fieldDefinition);
        }

        //获取这个bean中的所有方法
        Method[] methods = ReflectUtil.getMethods(beanClass);
        for (Method method : methods) {
            MethodDefinition methodDefinition = dealMethodDefinition(method, beanDefinition);
            if (methodDefinition == null) {
                continue;
            }
            List<MethodDefinition> methodDefinitions = beanDefinition.getAnnotationMethodDefinitions();
            if (methodDefinitions == null) {
                methodDefinitions = CollUtil.newArrayList();
                beanDefinition.setAnnotationMethodDefinitions(methodDefinitions);
            }
            methodDefinitions.add(methodDefinition);
        }

        return beanDefinition;
    }


    private static FieldDefinition dealFieldDefinition(Field field, BeanDefinition beanDefinition, AnnoUtil annoUtil) {
        //获取字段上的所有注解
        Annotation[] annotations = AnnotationUtil.getAnnotations(field, true);
        //校验排除一些注解
        Map<Class<? extends Annotation>, AnnotationHandler> fieldAnnoAndHandlerMap =
                annoUtil.getFieldAnnoAndHandlerMap();
        List<Annotation> collect = CollUtil.newArrayList(annotations)
                .stream()
                .filter(annotation -> !AnnoUtil.excludeAnnotation.contains(annotation.annotationType()))
                .filter(annotation -> fieldAnnoAndHandlerMap.get(annotation.annotationType()) != null)
                .collect(Collectors.toList());


        if (CollUtil.isNotEmpty(collect)) {
            FieldDefinition fieldDefinition = new FieldDefinition();
            fieldDefinition.setBeanDefinition(beanDefinition);
            fieldDefinition.setFieldName(field.getName());
            fieldDefinition.setFieldType(field.getType());
            fieldDefinition.setAnnotatedElement(new CombinationAnnotationElement(field));
            return fieldDefinition;
        }
        return null;
    }


    private static MethodDefinition dealMethodDefinition(Method method, BeanDefinition beanDefinition, AnnoUtil annoUtil) {
        //获取字段上的所有注解
        Annotation[] annotations = AnnotationUtil.getAnnotations(method, true);
        //校验排除一些注解
        Map<Class<? extends Annotation>, AnnotationHandler> methodAnnoAndHandlerMap = annoUtil.getMethodAnnoAndHandlerMap();
        List<Annotation> collect = CollUtil.newArrayList(annotations)
                .stream()
                .filter(annotation -> !AnnoUtil.excludeAnnotation.contains(annotation.annotationType()))
                .filter(annotation -> methodAnnoAndHandlerMap.get(annotation.annotationType()) != null)
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(collect)) {
            MethodDefinition methodDefinition = new MethodDefinition();
            methodDefinition.setBeanDefinition(beanDefinition);
            methodDefinition.setMethodName(method.getName());
            methodDefinition.setReturnType(method.getReturnType());
            ParamDefinition[] methodParamsAndAnnotaton = AsmUtil.getMethodParamsAndAnnotaton(method);
            methodDefinition.setParamDefinitions(methodParamsAndAnnotaton);
            methodDefinition.setAnnotatedElement(new CombinationAnnotationElement(method));
            return methodDefinition;
        }
        return null;

    }


}
