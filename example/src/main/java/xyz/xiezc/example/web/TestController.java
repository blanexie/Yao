package xyz.xiezc.example.web;

import cn.hutool.json.JSONUtil;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.PostMapping;
import xyz.xiezc.ioc.starter.web.annotation.RequestBody;

@Controller("/")
public class TestController {


    @PostMapping("/test.json")
    public String get(@RequestBody  Album param) {
        return JSONUtil.toJsonStr(param);
    }
}
