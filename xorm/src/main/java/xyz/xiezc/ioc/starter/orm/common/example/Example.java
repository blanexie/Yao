package xyz.xiezc.ioc.starter.orm.common.example;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Example {

    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer limit;

    protected Long offset;

    protected Integer pageNo;

    protected Integer pageSize = 10;

    /**
     * 实体对象的类型
     */

    protected Class<?> entityClass;


    static ThreadLocal<Example> threadLocal = new ThreadLocal<>();


    public static Criteria of(Class<?> entityClass) {
        Example example = threadLocal.get();
        if (example == null) {
            example = new Example();
            threadLocal.set(example);
        }
        example.clear();
        example.entityClass = entityClass;
        Criteria criteria = example.createCriteria(example);
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
        entityClass = null;
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