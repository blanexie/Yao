package xyz.xiezc.ioc.common.create;

import xyz.xiezc.ioc.BeanCreateUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

/**
 * 实现类需要
 */
public interface BeanCreateStrategy {

    void createBean(BeanDefinition beanDefinition);

    BeanTypeEnum getBeanTypeEnum();

    void setBeanCreateUtil(BeanCreateUtil beanCreateUtil);

}
