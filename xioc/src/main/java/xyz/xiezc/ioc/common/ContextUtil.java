package xyz.xiezc.ioc.common;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import lombok.Data;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.BeanSignature;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

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
     * 容器
     */
    Set<BeanDefinition> context = new HashSet<>();

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
        context.add(beanDefinition);
    }

    /**
     * 从容器中获取对应的bean
     *
     * @param beanSignature
     * @return
     */
    public BeanDefinition getComplatedBeanDefinitionBySignature(BeanSignature beanSignature) {
        BeanDefinition beanDefinition = getBeanDefinitionBySignature(beanSignature);
        if (beanDefinition == null) {
            new RuntimeException("容器中不存在对应的bean：" + beanSignature.toString());
        }
        if (beanDefinition.getBeanStatus() == BeanStatusEnum.Completed) {
            return beanDefinition;
        }
        throw new RuntimeException("bean的依赖不足， bean: " + beanDefinition.toString());
    }


    /**
     * 从容器中获取对应的bean
     *
     * @param beanSignature
     * @return 容器中不存在返回null，
     */
    public BeanDefinition getBeanDefinitionBySignature(BeanSignature beanSignature) {
        Optional<BeanDefinition> first = context.stream()
                .filter(beanDefinition -> {
                    Class<?> beanClass = beanDefinition.getBeanClass();
                    if (beanSignature.getBeanClass() == beanClass) {
                        return true;
                    }
                    if (ClassUtil.isAssignable(beanSignature.getBeanClass(), beanClass)) {
                        return true;
                    }
                    return false;
                })
                .findFirst();
        return first.orElse(null);
    }


    /**
     * 从容器中获取对应的bean
     *
     * @param beanSignature
     * @return 容器中不存在返回null，
     */
    public List<BeanDefinition> getBeanDefinitions(BeanSignature beanSignature) {
        List<BeanDefinition> collect = context.stream()
                .filter(beanDefinition -> {
                    Class<?> beanClass = beanDefinition.getBeanClass();
                    if (beanSignature.getBeanClass() == beanClass) {
                        return true;
                    }
                    if (ClassUtil.isAssignable(beanSignature.getBeanClass(), beanClass)) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());

        return collect;
    }

    /**
     * 从容器中获取对应的bean
     *
     * @param clazz
     * @return 容器中不存在返回null，
     */
    public List<BeanDefinition> getBeanDefinitions(Class<?> clazz) {
        BeanSignature beanSignature = new BeanSignature();
        beanSignature.setBeanTypeEnum(BeanTypeEnum.bean);
        beanSignature.setBeanClass(clazz);
        beanSignature.setBeanName(clazz.getName());
        return getBeanDefinitions(beanSignature);
    }
}
