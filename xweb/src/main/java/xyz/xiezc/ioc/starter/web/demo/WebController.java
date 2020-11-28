package xyz.xiezc.ioc.starter.web.demo;


import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;

@Controller("web")
public class WebController {


    @GetMapping("/test")
    public Object test(int a) {
        return a;
    }
}
