package xyz.xiezc.ioc.starter.orm.common.example;

import cn.hutool.db.Db;
import cn.hutool.db.dialect.DriverUtil;
import lombok.Getter;
import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.orm.util.XormDbUtil;
import xyz.xiezc.ioc.starter.orm.xml.MapperDefine;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public class Example {


    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer limit;

    protected Long offset;

    protected  String dbType;

    /**
     * 关联的对象的信息
     */
    MapperDefine mapperDefine;
    /**
     * 根据这个处理不同数据库的sql的差异性
     */


    static ThreadLocal<Example> threadLocal = new ThreadLocal<>();

    public static Map<Class<?>, MapperDefine> mapperDefineMap = new HashMap<>();

    public static Criteria of(Class<?> entityClass) {
        Example example = threadLocal.get();
        if (example == null) {
            example = new Example();
            threadLocal.set(example);
        }
        example.clear();
        MapperDefine mapperDefine = mapperDefineMap.get(entityClass);
        if (mapperDefine == null) {
            throw new RuntimeException("Example请传入正确的实体对象类");
        }
        example.mapperDefine = mapperDefine;
        //如果多数据源的话这里需要修改
        BeanDefinition beanDefinition = Xioc.getApplicationContext().getBeanDefinitionContext().getBeanDefinition(DataSource.class);
        Object bean = beanDefinition.getBean();
        String driverClassStr = DriverUtil.identifyDriver((DataSource) bean);
        example.dbType = XormDbUtil.getDbType(driverClassStr);

        Criteria criteria = example.createCriteria(example);
        return criteria;
    }

    public void clear() {
        /**
         * 清理掉，方便支持多数据源的处理
         */
        dbType = null;
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
        mapperDefine = null;
        limit = null;
        offset = null;
    }

    protected Criteria createCriteria(Example example) {
        Criteria criteria = createCriteriaInternal(example);
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal(Example example) {
        Criteria criteria = new Criteria(example);
        return criteria;
    }


    private Example() {
        oredCriteria = new ArrayList<Criteria>();
    }


}