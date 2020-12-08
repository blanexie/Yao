package xyz.xiezc.example.web.repository;

import xyz.xiezc.example.web.entity.UserDO;
import xyz.xiezc.ioc.starter.orm.annotation.Query;
import xyz.xiezc.ioc.starter.orm.annotation.Repository;
import xyz.xiezc.ioc.starter.orm.core.BasicRepository;

import java.util.List;

/**
 *
 */
@Repository
public interface UserReposotpry extends BasicRepository {

    @Query("from UserDO")
    List<UserDO> findAll();

    @Query("from UserDO where id =:id")
    UserDO findById(Integer id);

}
