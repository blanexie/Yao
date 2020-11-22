package xyz.xiezc.ioc.example;


import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.annotation.cron.Cron;

import java.time.LocalTime;

@Data
@Configuration
public class Demo {

    @Cron("*/5 * * * * *")
    public void test() {
        System.out.println(LocalTime.now());
    }

    private Integer s = 10;

    public static void main(String[] args) {

    }
}
