package xyz.xiezc.ioc.starter.common.definition;


public interface FactoryBean<T> {

    /**
     * 返回创建好的对象
     */
    T getObject() ;

    /**
     * 返回需要创建的对象的类型
     */
    Class<T> getObjectType();


    default boolean isSingleton() {
        return true;
    }

}
