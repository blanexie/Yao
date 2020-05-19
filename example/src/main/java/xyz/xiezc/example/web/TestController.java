package xyz.xiezc.example.web;

import cn.hutool.json.JSONUtil;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.starter.orm.common.example.Example;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.web.entity.WebContext;

import java.util.List;
import java.util.Map;

@Controller("/")
public class TestController {


    @Inject
    AlbumMapper albumMapper;

    @GetMapping("/test.json")
    public String get(String param) {
        WebContext webContext = WebContext.get();


        Example build = Example.of(Album.class).andIdEqualTo(3537).build();
        List<Album> albums = albumMapper.selectByExample(build);

        Map<String, Object> session = webContext.getSession();
        Object param1 = session.get("param");
        session.put("param", param);

        return JSONUtil.toJsonStr( albums);
    }
}
