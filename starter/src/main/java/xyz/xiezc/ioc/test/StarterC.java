package xyz.xiezc.ioc.test;


import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;

@Controller("start")
public class StarterC {


    @GetMapping("print")
    public String print(String z) {
        return "200 OK" + z;
    }


}
