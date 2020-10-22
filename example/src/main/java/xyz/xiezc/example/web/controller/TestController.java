package xyz.xiezc.example.web.controller;

import cn.hutool.json.JSONUtil;
import xyz.xiezc.example.web.common.PropertiesConfig;
import xyz.xiezc.example.web.common.TestAopspect;
import xyz.xiezc.example.web.entity.AlbumDO;
import xyz.xiezc.example.web.mapper.AlbumMapper;
import xyz.xiezc.example.web.service.TestService;
import xyz.xiezc.ioc.starter.annotation.aop.Aop;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.orm.common.example.Example;
import xyz.xiezc.ioc.starter.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.starter.web.entity.WebContext;

import java.util.List;
import java.util.Map;

@Aop(TestAopspect.class)
@Controller("/")
public class TestController {

    @Autowire
    AlbumMapper albumMapper;
    @Autowire
    TestService[] testServices;

    @Autowire
    PropertiesConfig propertiesConfig;

    @GetMapping("/get.json")
    public String get() {
        return JSONUtil.toJsonStr(propertiesConfig);
    }


    @GetMapping("/queryById.json")
    public String queryById(Integer id) {
        AlbumDO album = albumMapper.queryById(id);
        return JSONUtil.toJsonStr(album);
    }

    @GetMapping("/test.json")
    public String get(String param) {
        WebContext webContext = WebContext.get();
        //
        Example build = Example.of(AlbumDO.class)
                .andEqualTo(AlbumDO::getId, 3537) //支持类似mybatis-plus的lambda的使用方式
                .build();
        List<AlbumDO> albums = albumMapper.selectByExample(build);
        //获取session信息
        Map<String, Object> session = webContext.getSession();
        session.put("param", param);
        return JSONUtil.toJsonStr(albums);
    }
}
