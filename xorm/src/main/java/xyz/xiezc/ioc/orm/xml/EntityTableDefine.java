package xyz.xiezc.ioc.orm.xml;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.orm.common.example.Example;

import java.util.HashSet;
import java.util.Set;

@Data
public class EntityTableDefine {

    public static final Class ExampleName = Example.class;

    /**
     * 表
     */
    private ColumnProp table;
    private ColumnProp id;
    private Set<ColumnProp> columns=new HashSet<>();


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
