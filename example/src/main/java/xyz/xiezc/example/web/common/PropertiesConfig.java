package xyz.xiezc.example.web.common;

import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.annotation.core.Value;

/**
 * @Description TODO
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/29 11:59 上午
 **/
@Data
@Configuration
public class PropertiesConfig {


    @Value("server.port")
    private String port;


}