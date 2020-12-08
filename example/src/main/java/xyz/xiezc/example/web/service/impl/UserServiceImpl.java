package xyz.xiezc.example.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.xiezc.example.web.entity.UserDO;
import xyz.xiezc.example.web.repository.UserReposotpry;
import xyz.xiezc.example.web.service.UserService;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Component;

import java.util.List;

/**
 * @author xiezc
 */
@Component
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowire
    UserReposotpry userReposotpry;

    @Override
    public List<UserDO> findAllUser() {
        log.info("UserServiceImpl findAllUser");
        return userReposotpry.findAll();
    }

    @Override
    public  UserDO  findById(Integer id) {
        log.info("UserServiceImpl findById");
        return userReposotpry.findById(id);
    }
}