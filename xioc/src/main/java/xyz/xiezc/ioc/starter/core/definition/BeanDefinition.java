package xyz.xiezc.ioc.starter.core.definition;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 每个bean类的描述信息
 *
 * @author xiezc
 * @date 2019/03/29 14:24
 */
@Setter
@Getter
public class BeanDefinition {

    Log log = LogFactory.get(BeanDefinition.class);

    /**
     * bean的class
     */
    private Class<?> beanClass;

    /**
     * methodBean 的类型的 bean调用的方法
     *
     * @Bean
     */
    private MethodDefinition invokeMethodBean;

    /**
     * bean ： 类上的注解，放入容器的bean
     * factoryBean,
     * methodBean
     * properties ： 配置的注入
     */
    private BeanTypeEnum beanTypeEnum;

    /**
     * 这个bean的状态
     */
    private BeanStatusEnum beanStatus = BeanStatusEnum.Original;


    /**
     * 具体的实例, 当beanScopeEnum为methodBean的时候，要注意下这个值是方法的返回值
     */
    private Object bean;

    /**
     * 返回具体的bean实例
     *
     * @param <T>
     * @return
     */
    public <T> T getBean() {
        return (T) this.bean;
    }

}
