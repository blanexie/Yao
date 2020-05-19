package xyz.xiezc.example.web;

import cn.hutool.json.JSONUtil;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.web.annotation.PostMapping;
import xyz.xiezc.ioc.starter.web.annotation.RequestBody;
import xyz.xiezc.ioc.starter.web.entity.WebContext;

import java.util.Map;

@Controller("/")
public class TestController {


    @GetMapping("/test.json")
    public String get(String param) {
        WebContext webContext = WebContext.get();

        Map<String, Object> session = webContext.getSession();
        Object param1 = session.get("param");
        session.put("param", param);

        return JSONUtil.toJsonStr( param1+" |||| "+param);
    }
}
