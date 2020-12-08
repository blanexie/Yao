package xyz.xiezc.example.web.service;

import xyz.xiezc.example.web.entity.UserDO;

import java.util.List;

public interface UserService {

   List<UserDO> findAllUser();

    UserDO findById(Integer id);
}
