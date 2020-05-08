package xyz.xiezc.ioc.starter.orm.xml;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.SqlSessionFactory;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.definition.FactoryBean;
import xyz.xiezc.ioc.starter.orm.common.BaseMapper;

public class MapperFactoryBean<T extends BaseMapper> implements FactoryBean<T> {

    @Setter
    @Getter
    Class<T> mapperInterface;

    @Inject
    SqlSessionFactory sqlSessionFactory;

    @Override
    public T getObject() throws Exception {
        return sqlSessionFactory.openSession(true).getMapper(mapperInterface);
    }
}
