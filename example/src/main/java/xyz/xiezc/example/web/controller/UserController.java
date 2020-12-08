package xyz.xiezc.example.web.controller;

import cn.hutool.json.JSONUtil;
import xyz.xiezc.example.web.common.PropertiesConfig;
import xyz.xiezc.example.web.entity.UserDO;
import xyz.xiezc.example.web.repository.UserReposotpry;
import xyz.xiezc.example.web.service.UserService;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.web.annotation.PostMapping;
import xyz.xiezc.ioc.starter.web.annotation.RequestBody;

import java.util.List;


/**
 * @author xiezc
 */
@Controller("/")
public class UserController {


    @Autowire
    UserService userServices;

    @Autowire
    UserReposotpry userReposotpry;

    @Autowire
    PropertiesConfig propertiesConfig;

    @GetMapping("/get.json")
    public String get() {
        return JSONUtil.toJsonStr(propertiesConfig);
    }


    @GetMapping("/queryById.json")
    public UserDO queryById(Integer id) {
        UserDO byId = userServices.findById(id);
        return byId;
    }

    @GetMapping("/queryAll.json")
    public List<UserDO> queryAll() {
        List<UserDO> all = userReposotpry.findAll();
        return all;
    }

    @PostMapping("/addNew.json")
    public UserDO get(@RequestBody UserDO param) {
        userReposotpry.save(param);
        return param;
    }
}
