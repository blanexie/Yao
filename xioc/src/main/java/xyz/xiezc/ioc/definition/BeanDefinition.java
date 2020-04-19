package xyz.xiezc.ioc.definition;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.enums.BeanStatusEnum;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 每个bean类的描述信息
 *
 * @author wb-xzc291800
 * @date 2019/03/29 14:24
 */
@Setter
@Getter
public class BeanDefinition extends BeanSignature  {

    Log log = LogFactory.get(BeanDefinition.class);

    /**
     * 是否是单例模式。 默认是单例的
     */
    private boolean isSingleton = true;

    /**
     *
     */
    private BeanStatusEnum beanStatus = BeanStatusEnum.Original;

    /**
     * 需要注入的字段， 这个字段不一定是一个配置， 有可能是一个配置
     */
    private List<BeanSignature> injectBeans;

    /**
     *
     */
    private BeanDefinition parentBeanDefinition;

    /**
     * 构造bean的方法
     */
    private Method method;

    /**
     * 具体的实例
     */
    private Object bean;

    /**
     * 返回具体的bean实例
     *
     * @param <T>
     * @return
     */
    public <T> T getBean() {
        return (T) bean;
    }

    public void setBeanSignature(BeanSignature beanSignature) {
        this.setBeanName(beanSignature.getBeanName());
        this.setBeanClass(beanSignature.getBeanClass());
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


}
