package xyz.xiezc.ioc.definition;


public interface FactoryBean<T> {

    T getObject() throws Exception;

    default boolean isSingleton() {
        return true;
    }

}
