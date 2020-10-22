package xyz.xiezc.example;


import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.annotation.cron.Cron;
import xyz.xiezc.ioc.starter.annotation.cron.EnableCron;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;

import java.time.LocalTime;

@MapperScan({"xyz.xiezc.example.web.mapper"})
@Configuration
@EnableCron
public class Application {

    /**
     * 每3秒执行一次。<br>
     * 表达式每部分的意思如下：
     * 秒 分 时 日 月 星期
     */
    @Cron("*/3 * * * * *")
    public void cronTest() {
        System.out.println("定时任务执行: " + LocalTime.now());
    }

    public static void main(String[] args) {
        Xioc.run(Application.class);
    }
}
