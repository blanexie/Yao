package xyz.xiezc.ioc.starter.core.context;

/**
 * @Description
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 10:36 上午
 **/
public interface ApplicationContext extends BeanFactory {
    /**
     * 整个框架的核心启动方法
     *
     * @param clazz 启动类
     * @return
     */
    ApplicationContext run(Class clazz);

}