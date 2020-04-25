package xyz.xiezc.ioc.common.create;

import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

public interface BeanFactoryStrategy {

    void createBean(BeanDefinition beanDefinition);

    BeanTypeEnum getBeanTypeEnum();

    void setBeanFactoryUtil(BeanFactoryUtil beanFactoryUtil);

}
