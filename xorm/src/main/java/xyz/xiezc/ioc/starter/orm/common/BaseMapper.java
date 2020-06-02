package xyz.xiezc.ioc.starter.orm.common;

import org.apache.ibatis.annotations.Param;
import xyz.xiezc.ioc.starter.orm.common.example.Example;

import java.util.List;

/**
 * DAO公共基类，由MybatisGenerator自动生成请勿修改
 * @param <Model> The Model Class 这里是泛型不是Model类
 */
public interface BaseMapper<Model> {
    long countByExample(Example example);

    int deleteByExample(Example example);

    int deleteByPrimaryKey(Number id);

    int insert(Model record);

    int insertSelective(Model record);

    List<Model> selectByExample(Example example);

    Model selectByPrimaryKey(Number id);

    int updateByExampleSelective(@Param("record") Model record, @Param("example") Example example);

    int updateByExample(@Param("record") Model record, @Param("example") Example example);

    int updateByPrimaryKeySelective(Model record);

    int updateByPrimaryKey(Model record);
}