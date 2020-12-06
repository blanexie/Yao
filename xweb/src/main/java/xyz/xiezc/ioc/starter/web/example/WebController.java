package xyz.xiezc.ioc.starter.web.example;


import lombok.Getter;
import lombok.Setter;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.web.annotation.PostMapping;
import xyz.xiezc.ioc.starter.web.annotation.RequestBody;

@Controller("web")
public class WebController {


    @GetMapping("/test")
    public Object test(int a) {
        return a;
    }
    @PostMapping("/testa")
    public Object testa(int a) {
        return a;
    }

    @PostMapping("/user")
    public Object test(@RequestBody User a) {
        return a;
    }

    @Setter
    @Getter
    static class User{
        int age;
        String name;
    }
}
