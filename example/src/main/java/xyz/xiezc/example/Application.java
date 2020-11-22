package xyz.xiezc.example;


import xyz.xiezc.ioc.annotation.core.Configuration;
import xyz.xiezc.ioc.annotation.cron.Cron;
import xyz.xiezc.ioc.orm.annotation.MapperScan;

import java.time.LocalTime;

@MapperScan({"xyz.xiezc.example.web.mapper"})
@Configuration
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


}
