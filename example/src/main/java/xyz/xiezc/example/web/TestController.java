package xyz.xiezc.example.web;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import xyz.xiezc.ioc.annotation.Init;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.starter.orm.common.Example;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.web.annotation.PostMapping;

import java.util.List;

@Data
@Controller("/test")
public class TestController {

    @Inject
    AlbumMapper albumMapper;

    @PostMapping("testPost")
    public String testPost(int per) {
        return "hello word " + per;
    }

    @GetMapping("testGet")
    public String testGet(Integer id) {
        Example.Criteria criteria = Example.of().createCriteria().andEqualTo(Album::getId, 3450);
        List<Album> album = albumMapper.selectByExample(criteria.build());
        return JSONUtil.toJsonStr(album);
    }
}
