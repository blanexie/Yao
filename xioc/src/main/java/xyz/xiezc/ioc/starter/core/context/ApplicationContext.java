package xyz.xiezc.ioc.starter.core.context;

import xyz.xiezc.ioc.starter.event.EventDispatcher;

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

    /**
     * 获取事件分发器
     *
     * @return
     */
    EventDispatcher getEventDispatcher();


    void clearContext();


    void loadProperties();


    void loadBeanDefinition(String... packageNames);

    /**
     * 在正式启动之前进行一些操作， 这个方法一般留第三方扩展实现
     */
    void beforeInvoke();

    /**
     * 调用所有的BeanFactoryPostProcess实现类的钩子方法
     * <p>
     * 会先实例化每个实现类(只是实例化，并不会注入依赖)， 再排序实现类， 最后逐个调用
     */
    void invokeBeanFactoryPostProcess();

    /**
     * 初始化BeanPostProcess的实现类， 这里是正常的初始化，会进行依赖注入和init方法的调用
     */
     void initBeanPostProcess();

    /**
     * 此处会加载事件分发器， 同时也会初始化事件监听器。
     * <p>
     * 会先查找容器中有无事件分发器， 如果没有则使用框架默认的事件分发器
     */
     void loadListener();

    void initAllBeans();
    /**
     * 最后结束的钩子方法
     */
     void finish();
}