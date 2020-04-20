package xyz.xiezc.ioc.definition;


public interface FactoryBean<T> {

    T getObject() throws Exception;

    Class<T> getObjectType();

    default boolean isSingleton() {
        return true;
    }

}
