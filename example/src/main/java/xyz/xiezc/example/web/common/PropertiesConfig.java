package xyz.xiezc.example.web.common;

import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.Configuration;
import xyz.xiezc.ioc.starter.annotation.PropertyInject;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/29 11:59 上午
 **/
@Data
@Configuration
@PropertyInject(prefix = "xweb.server")
public class PropertiesConfig {


    private String port;
    private String path;


}