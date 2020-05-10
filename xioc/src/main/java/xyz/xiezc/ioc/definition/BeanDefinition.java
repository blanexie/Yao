package xyz.xiezc.ioc.definition;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Set;

/**
 * 每个bean类的描述信息
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:24
 */
@Setter
@Getter
public class BeanDefinition {

    Log log = LogFactory.get(BeanDefinition.class);

    /**
     * 这个bean上面的所有注解
     */
    AnnotatedElement annotatedElement;

    /**
     * 默认都是单例的
     */
    private boolean isSingleton = true;

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
     * bean paramName
     */
    private String beanName;

    /**
     * bean的class
     */
    private Class<?> beanClass;

    /**
     * 有自定义注解的字段
     */
    private Set<FieldDefinition> annotationFiledDefinitions;

    /**
     * 有自定义注解的方法
     */
    private Set<MethodDefinition> annotationMethodDefinitions;

    /**
     * 需要在初始化完成后进行调用的方法
     *
     * @Init
     */
    private MethodDefinition initMethodDefinition;

    /**
     * methodBean 的类型的bean调用的方法
     *
     * @Bean
     */
    private MethodDefinition invokeMethodBean;

    /**
     * 具体的实例, 当beanScopeEnum为methodBean的时候，要注意下这个值是方法的返回值
     */
    private Object bean;


    public <T> T getFactoryBean() {
        if (getBeanTypeEnum() == BeanTypeEnum.factoryBean) {
            return (T) bean;
        }
        return null;
    }

    /**
     * 返回具体的bean实例
     *
     * @param <T>
     * @return
     */
    public <T> T getBean() {
        if (bean == null) {
            Xioc.getApplicationContext().createBean(this);
        }
        if (getBeanTypeEnum() == BeanTypeEnum.factoryBean) {
            return (T) ((FactoryBean) this.bean).getObject();
        }
        return (T) this.bean;
    }

    public boolean checkBean() {
        if (StrUtil.isBlank(this.getBeanName())) {
            log.error("请设置正确的beanName，  bean:{}", this.toString());
            return false;
        }
        if (getBeanClass() == null) {
            log.error("请设置正确的beanClass，  bean:{}", this.toString());
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                " isSingleton=" + isSingleton +
                ", beanName='" + getBeanName() + '\'' +
                ", objClass=" + getBeanClass() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeanDefinition)) return false;
        BeanDefinition that = (BeanDefinition) o;
        return getBeanTypeEnum() == that.getBeanTypeEnum() &&
                Objects.equals(getBeanName(), that.getBeanName()) &&
                Objects.equals(getBeanClass(), that.getBeanClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBeanTypeEnum(), getBeanName(), getBeanClass());
    }
}
