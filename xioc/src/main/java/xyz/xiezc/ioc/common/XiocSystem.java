package xyz.xiezc.ioc.common;


/**
 * 系统类的初始化接口。 实现了这个接口的bean， 都会调用这个接口来初始化。 这个接口的初始化会在前面
 */
public interface XiocSystem {

    void newInstance();

}
