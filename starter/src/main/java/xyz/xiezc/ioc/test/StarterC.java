package xyz.xiezc.ioc.test;


import cn.hutool.json.JSONObject;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.PostMapping;
import xyz.xiezc.ioc.starter.web.annotation.RequestBody;

@Controller("start")
public class StarterC {

    @PostMapping("print")
    public JSONObject print(@RequestBody JSONObject h) {
        WebContext webContext = WebContext.get();
        return h;
    }


}
