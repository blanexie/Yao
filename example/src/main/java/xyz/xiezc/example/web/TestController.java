package xyz.xiezc.example.web;

import xyz.xiezc.web.annotation.Controller;
import xyz.xiezc.web.annotation.GetMapping;
import xyz.xiezc.web.annotation.PostMapping;
import xyz.xiezc.web.annotation.RequestBody;

import java.util.List;

@Controller("/test")
public class TestController {

    @PostMapping("testPost")
    public String testPost(@RequestBody  List<Integer> per) {
        return "hello word " + per;
    }

    @GetMapping("testGet")
    public String testGet(Integer[] per) {
        return "hello word------" + per;
    }
}
