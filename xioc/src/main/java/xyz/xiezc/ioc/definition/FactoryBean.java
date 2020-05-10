package xyz.xiezc.ioc.definition;


public interface FactoryBean<T> {

    T getObject();

    default boolean isSingleton() {
        return true;
    }

}
