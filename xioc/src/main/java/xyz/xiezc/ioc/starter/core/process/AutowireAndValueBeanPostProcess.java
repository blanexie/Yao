package xyz.xiezc.ioc.starter.core.process;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.lang.reflect.Field;

/**
 * @Description 处理@Autowire和@Value注解
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 3:18 下午
 **/
public class AutowireAndValueBeanPostProcess implements BeanPostProcess {


    @Override
    public boolean beforeInstance(ApplicationContext applicationContext, BeanDefinition beanDefinition) {


        return true;
    }

    @Override
    public boolean beforeInit(ApplicationContext applicationContext, BeanDefinition beanDefinition) {
        BeanStatusEnum beanStatus = beanDefinition.getBeanStatus();
        Assert.isTrue(beanStatus == BeanStatusEnum.HalfCooked, "Bean类初始化的状态不对");

        BeanTypeEnum beanTypeEnum = beanDefinition.getBeanTypeEnum();
        //普通bean类
        if (beanTypeEnum == BeanTypeEnum.bean) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Field[] fields = ReflectUtil.getFields(beanClass);

            for (Field field : fields) {
                Autowire annotation = AnnotationUtil.getAnnotation(field, Autowire.class);
                if (annotation == null) {
                    continue;
`j
33`，                Class<?> type = field.getType();
                BeanDefinition beanDefinition1 = applicationContext.getBean(type);

            }

        }


        return true;
    }

    @Override
    public boolean afterInit(ApplicationContext applicationContext, BeanDefinition beanDefinition) {
        return true;
    }
}