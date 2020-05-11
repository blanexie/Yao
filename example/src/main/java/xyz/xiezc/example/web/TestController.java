package xyz.xiezc.example.web;

import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;

@Controller("/")
public class TestController {


    @GetMapping("/test.json")
    public String get(String param) {
        return param;
    }
}
