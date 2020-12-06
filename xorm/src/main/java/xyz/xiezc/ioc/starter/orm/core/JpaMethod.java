package xyz.xiezc.ioc.starter.orm.core;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Setter
@Getter
public class JpaMethod {
    Class clazz;
    String sql;
    Method method;
    String[] params;

    boolean isSave=false;
}
