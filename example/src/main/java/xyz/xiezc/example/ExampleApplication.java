package xyz.xiezc.example;


import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;

@MapperScan("xyz.xiezc.example.web")
@Configuration
public class ExampleApplication {

    public static void main(String[] args) {
        Xioc.run(ExampleApplication.class);
    }
}
