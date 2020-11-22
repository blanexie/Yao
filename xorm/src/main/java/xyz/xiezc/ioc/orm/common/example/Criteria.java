package xyz.xiezc.ioc.orm.common.example;

import java.util.List;

/**
 *
 */
public class Criteria extends GeneratedCriteria {

    protected Criteria(Example example) {
        super(example);
    }

    public void or(Criteria criteria) {
        example.oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = example.createCriteriaInternal(example);
        example.oredCriteria.add(criteria);
        return criteria;
    }


    public Criteria orderByClause(String orderByClause) {
        example.orderByClause = orderByClause;
        return this;
    }

    public Criteria distinct() {
        example.distinct = true;
        return this;
    }

    public Criteria or(List<Criteria> oredCriteria) {
        example.oredCriteria.addAll(oredCriteria);
        return this;
    }

    public Criteria limit(Integer limit) {
        example.limit = limit;
        return this;
    }

    public Criteria offset(Long offset) {
        example.offset = offset;
        return this;
    }

}
