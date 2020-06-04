package xyz.xiezc.example.web;

import cn.hutool.json.JSONUtil;
import xyz.xiezc.ioc.system.annotation.Aop;
import xyz.xiezc.ioc.system.annotation.Inject;
import xyz.xiezc.ioc.starter.orm.common.example.Example;
import xyz.xiezc.ioc.starter.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.starter.web.entity.WebContext;

import java.util.List;
import java.util.Map;

@Aop(TestAopspect.class)
@Controller("/")
public class TestController {

    @Inject
    AlbumMapper albumMapper;
    @Inject
    TestA[] testAS;

    @GetMapping("/get.json")
    public String get1() {
        return testAS.length + "";
    }


    @GetMapping("/test.json")
    public String get(String param) {
        WebContext webContext = WebContext.get();
        //
        Example build = Example.of(Album.class)
                .andEqualTo(Album::getId, 3537) //支持类似mybatis-plus的lambda的使用方式
                .build();
        List<Album> albums = albumMapper.selectByExample(build);
        //获取session信息
        Map<String, Object> session = webContext.getSession();
        session.put("param", param);

        return JSONUtil.toJsonStr(albums);
    }
}
