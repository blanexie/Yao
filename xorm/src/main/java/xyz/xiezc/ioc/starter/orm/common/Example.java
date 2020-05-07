package xyz.xiezc.ioc.starter.orm.common;

import lombok.Getter;
import xyz.xiezc.ioc.starter.orm.lambda.LambdaUtils;
import xyz.xiezc.ioc.starter.orm.lambda.SFunction;
import xyz.xiezc.ioc.starter.orm.xml.EntityTableDefine;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Example {

    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    private Integer limit;

    private Long offset;

    private Integer pageNo;

    private Integer pageSize = 10;

    static ThreadLocal<Example> threadLocal = new ThreadLocal<>();

    public static Example of() {
        Example example = threadLocal.get();
        if (example == null) {
            example = new Example();
            threadLocal.set(example);
        }
        example.clear();
        return example;
    }

    private Example() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    public Example setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
        return this;
    }

    public Example setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }


    public Example setOredCriteria(List<Criteria> oredCriteria) {
        this.oredCriteria = oredCriteria;
        return this;
    }

    public Example setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Example setPageSize(int pageSize) {
        this.limit = pageSize;
        this.pageSize = pageSize;
        this.offset = (pageNo - 1L) * pageSize;
        return this;
    }

    public Example setPageNo(int pageNo) {
        this.pageNo = pageNo;
        this.offset = (pageNo - 1L) * pageSize;
        return this;
    }

    public Example setOffset(Long offset) {
        this.offset = offset;
        return this;
    }


    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Number value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Number value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Number value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Number value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Number value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Number value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Number> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Number> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Number value1, Number value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Number value1, Number value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public <T> Criteria andIsNull(SFunction<T, ?> sFunction) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " is null");
            return (Criteria) this;
        }

        public <T> Criteria andIsNotNull(SFunction<T, ?> sFunction) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " is not null");
            return (Criteria) this;
        }

        public <T, V> Criteria andEqualTo(SFunction<T, ?> sFunction, V value) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " = ", value, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andNotEqualTo(SFunction<T, ?> sFunction, V value) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " <>", value, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andGreaterThan(SFunction<T, ?> sFunction, V value) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " >", value, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andGreaterThanOrEqualTo(SFunction<T, ?> sFunction, V value) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " >=", value, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andLessThan(SFunction<T, ?> sFunction, V value) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " <", value, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andLessThanOrEqualTo(SFunction<T, ?> sFunction, V value) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " <=", value, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andLike(SFunction<T, ?> sFunction, V value) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " like", value, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andNotLike(SFunction<T, ?> sFunction, V value) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " not like", value, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andIn(SFunction<T, ?> sFunction, List<V> values) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " in", values, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andNotIn(SFunction<T, ?> sFunction, List<V> values) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " not in", values, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andBetween(SFunction<T, ?> sFunction, V value1, V value2) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " between", value1, value2, columnProp.getProperty());
            return (Criteria) this;
        }

        public <T, V> Criteria andNotBetween(SFunction<T, ?> sFunction, V value1, V value2) {
            EntityTableDefine.ColumnProp columnProp = LambdaUtils.getColumnProp(sFunction);
            addCriterion(columnProp.getColumn() + " not between", value1, value2, columnProp.getProperty());
            return (Criteria) this;
        }

    }

    /**
     *
     */
    public static class Criteria extends GeneratedCriteria {

        public Example build() {
            return threadLocal.get();
        }

        protected Criteria() {
            super();
        }
    }


    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}