package xyz.xiezc.ioc.starter.core.process;

import xyz.xiezc.ioc.starter.core.context.ApplicationContext;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/10/22 2:19 下午
 **/
public interface BeanFactoryPostProcess {

    default int order() {
        return 0;
    }

    /**
     * 钩子方法，只在启动前调用一次
     */
    void process(ApplicationContext applicationContext);

}