package xyz.xiezc.ioc.system.common.context;

import xyz.xiezc.ioc.system.common.definition.BeanDefinition;

/**
 * bean创建相关的接口， 包含创建时候的循环依赖的判断。
 */
public interface BeanCreateContext {


    /**
     * * beanDefinition的初始化， 其初始化的过程较为复杂， 具体我分以下几步
     * * 1. 将这个bean放入待初始化集合中，这个主要是用来解决循环依赖的判断的
     * * 2. 获取这个bean中的用户自定义注解，
     * * 2. 校验这个bean的所有依赖在不在容器中， 包括配置文件中配置值（@Value）的依赖
     * * 3. 检查有无自定义注解，如果有的话，此时获取对应的annotationHandler来处理下
     * * 4. 开始初始化，并且注入字段值，修改对应状态
     * * 5. 判断有无切面，有切面的话，生成代理类替换生成的bean
     *
     * @param beanDefinition
     * @return
     */
    BeanDefinition createBean(BeanDefinition beanDefinition);


}
