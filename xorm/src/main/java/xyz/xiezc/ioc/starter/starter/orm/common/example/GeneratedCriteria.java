package xyz.xiezc.ioc.starter.starter.orm.common.example;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import org.jetbrains.annotations.NotNull;
import xyz.xiezc.ioc.starter.starter.orm.annotation.Id;
import xyz.xiezc.ioc.starter.starter.orm.lambda.LambdaUtils;
import xyz.xiezc.ioc.starter.starter.orm.lambda.SFunction;
import xyz.xiezc.ioc.starter.starter.orm.util.StringUtil;
import xyz.xiezc.ioc.starter.starter.orm.xml.EntityTableDefine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class GeneratedCriteria {

    protected Example example;

    String idStr;

    protected GeneratedCriteria(Example example) {
        super();
        criteria = new ArrayList<Criterion>();
        this.example = example;
        this.idStr = getIdColumn(example);
    }

    public Example build() {
        return example;
    }


    protected List<Criterion> criteria;


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
        addCriterion(idStr + " is null");
        return (Criteria) this;
    }

    @NotNull
    private String getIdColumn(Example example) {
        String idStr = "id";
        for (Field declaredField : ClassUtil.getDeclaredFields(example.entityClass)) {
            Id annotation = AnnotationUtil.getAnnotation(declaredField, Id.class);
            if (annotation != null) {
                if (StrUtil.isNotBlank(annotation.value())) {
                    idStr = annotation.value();
                } else {
                    String name = declaredField.getName();
                    idStr = StringUtil.underscoreName(name);
                }
                break;
            }
        }
        return idStr;
    }

    public Criteria andIdIsNotNull() {
        addCriterion(idStr + " is not null");
        return (Criteria) this;
    }

    public Criteria andIdEqualTo(Number value) {
        addCriterion(idStr + " =", value, "id");
        return (Criteria) this;
    }

    public Criteria andIdNotEqualTo(Number value) {
        addCriterion(idStr + " <>", value, "id");
        return (Criteria) this;
    }

    public Criteria andIdGreaterThan(Number value) {
        addCriterion(idStr + " >", value, "id");
        return (Criteria) this;
    }

    public Criteria andIdGreaterThanOrEqualTo(Number value) {
        addCriterion(idStr + " >=", value, "id");
        return (Criteria) this;
    }

    public Criteria andIdLessThan(Number value) {
        addCriterion(idStr + " <", value, "id");
        return (Criteria) this;
    }

    public Criteria andIdLessThanOrEqualTo(Number value) {
        addCriterion(idStr + " <=", value, "id");
        return (Criteria) this;
    }

    public Criteria andIdIn(List<Number> values) {
        addCriterion(idStr + " in", values, "id");
        return (Criteria) this;
    }

    public Criteria andIdNotIn(List<Number> values) {
        addCriterion(idStr + " not in", values, "id");
        return (Criteria) this;
    }

    public Criteria andIdBetween(Number value1, Number value2) {
        addCriterion(idStr + " between", value1, value2, "id");
        return (Criteria) this;
    }

    public Criteria andIdNotBetween(Number value1, Number value2) {
        addCriterion(idStr + " not between", value1, value2, "id");
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
