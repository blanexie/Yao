package xyz.xiezc.ioc.starter.orm.example;


import xyz.xiezc.ioc.starter.orm.annotation.Query;
import xyz.xiezc.ioc.starter.orm.annotation.Repository;
import xyz.xiezc.ioc.starter.orm.core.BasicRepository;

import java.util.List;

@Repository
public interface UserRepository extends BasicRepository<UserTest> {

    @Query("from UserTest")
    List<UserTest> find( );

}
