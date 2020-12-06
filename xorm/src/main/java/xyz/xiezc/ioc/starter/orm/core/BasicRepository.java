package xyz.xiezc.ioc.starter.orm.core;

import java.util.List;

public interface BasicRepository<T> {

    void save(T t);

    void save(List<T> data);

}
