package xyz.xiezc.ioc.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import lombok.Data;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanScopeEnum;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 容器，装载bean的容器
 */
@Data
public class ContextUtil {

    Log log = LogFactory.get(ContextUtil.class);


    /**
     * 所有的配置文件加载进入
     */
    Setting setting;

    /**
     * bean的name和class的映射关系
     */
    Map<String, Class<?>> nameAndClassMap = new HashMap<>();

    /**
     * bean的class与Beandefinition的映射关系
     */
    Map<Class<?>, BeanDefinition> classaAndBeanDefinitionMap = new HashMap<>();

    /**
     * 注解和注解处理器的映射关系
     */
    AnnoUtil annoUtil = new AnnoUtil();

    /**
     * 注入bean到容器中
     *
     * @param beanDefinition
     */
    public void addBeanDefinition(BeanDefinition beanDefinition) {
        if (!beanDefinition.checkBean()) {
            log.error("请注入正确的bean到容器中");
            throw new RuntimeException("请注入正确的bean到容器中, bean: " + beanDefinition.toString());
        }
        nameAndClassMap.put(beanDefinition.getBeanName(),beanDefinition.getBeanClass());
        classaAndBeanDefinitionMap.put(beanDefinition.getBeanClass(),beanDefinition);
    }

    public BeanDefinition getBeanDefinitions(String beanName) {
        Class<?> aClass = nameAndClassMap.get(beanName);
        if (aClass == null) {
            return null;
        }
        BeanDefinition beanDefinition = classaAndBeanDefinitionMap.get(aClass);
        return beanDefinition;
    }


    public List<BeanDefinition> getBeanDefinitions(Class<?> beanClass) {
        Set<Class<?>> classes = classaAndBeanDefinitionMap.keySet();
        List<BeanDefinition> collect = classes
                .stream()
                .filter(clazz -> ClassUtil.isAssignable(beanClass, clazz))
                .map(classaAndBeanDefinitionMap::get)
                .collect(Collectors.toList());
        return collect;
    }

    public BeanDefinition getBeanDefinition(String beanName, Class<?> beanClass) {
        Class<?> aClass = nameAndClassMap.get(beanName);
        if (aClass == null) {
            return null;
        }
        if (ClassUtil.isAssignable(beanClass, aClass)) {
            return classaAndBeanDefinitionMap.get(beanClass);
        }
        return null;
    }


    public BeanDefinition getBeanDefinition(String beanName) {
        Class<?> aClass = nameAndClassMap.get(beanName);
        if (aClass == null) {
            return null;
        }
        return getBeanDefinition(aClass);
    }

    public BeanDefinition getBeanDefinition(Class<?> beanClass) {
        //先获取最契合的
        BeanDefinition beanDefinition = classaAndBeanDefinitionMap.get(beanClass);
        if (beanDefinition != null) {
            return beanDefinition;
        }
        //再获取其他契合的
        List<BeanDefinition> beanDefinitions = this.getBeanDefinitions(beanClass);
        if (beanDefinitions.size() != 1) {
            //未找到抛出异常
            ExceptionUtil.wrapAndThrow(new RuntimeException(beanClass.getName() + "类型获取的BeanDefinition不存在或者有两个及以上"));
        }
        return beanDefinitions.get(0);
    }


}
