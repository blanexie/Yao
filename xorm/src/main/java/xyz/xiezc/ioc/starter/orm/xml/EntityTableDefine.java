package xyz.xiezc.ioc.starter.orm.xml;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.starter.orm.common.Example;

import java.util.Set;

@Data
public class EntityTableDefine {


    public static final Class ExampleName = Example.class;

    /**
     * 表
     */
    private ColumnProp table;
    private ColumnProp id;
    private Set<ColumnProp> columns;


    @Setter
    @Getter
    @EqualsAndHashCode
    public static class ColumnProp {
        /**
         * 字段的类型, 或者表的对应实体累的类型
         */
        Class<?> clazz;
        /**
         * 对应字段名
         */
        String column;
        /**
         * 对应JAVA的对象的名称
         */
        String property;
    }

}
