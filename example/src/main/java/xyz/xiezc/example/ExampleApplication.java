package xyz.xiezc.example;


import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.annotation.Configuration;
import xyz.xiezc.ioc.starter.annotation.Cron;
import xyz.xiezc.ioc.starter.annotation.EnableCron;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;

import java.time.LocalDateTime;
import java.time.LocalTime;

@MapperScan({"xyz.xiezc.example.web"})
@Configuration
@EnableCron
public class ExampleApplication {

    @Cron("*/2 * * * * *")
    public void test() {
        System.out.println(LocalTime.now());
    }

    public static void main(String[] args) {
        Xioc.run(ExampleApplication.class);
    }
}
