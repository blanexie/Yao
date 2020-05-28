package xyz.xiezc.example;


import xyz.xiezc.ioc.system.Xioc;
import xyz.xiezc.ioc.system.annotation.Configuration;
import xyz.xiezc.ioc.starter.starter.orm.annotation.MapperScan;

@MapperScan("xyz.xiezc.example.web")
@Configuration
public class ExampleApplication {

    public static void main(String[] args) {
        Xioc.run(ExampleApplication.class);
    }
}
