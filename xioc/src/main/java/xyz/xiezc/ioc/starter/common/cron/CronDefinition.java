package xyz.xiezc.ioc.starter.common.cron;

import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.Cron;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.definition.MethodDefinition;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/5 2:02 下午
 **/
@Data
public class CronDefinition {

    private BeanDefinition beanDefinition;

    private MethodDefinition methodDefinition;

    private Cron cron;

}