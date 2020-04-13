package xyz.xiezc.example.web;

import xyz.xiezc.web.annotation.Controller;
import xyz.xiezc.web.annotation.GetMapping;
import xyz.xiezc.web.annotation.PostMapping;

@Controller("/test")
public class TestController {


    @PostMapping("testPost")
    public String testPost() {
        return "hello word";
    }

    @GetMapping("testGet")
    public String testGet() {
        return "hello word";
    }
}
