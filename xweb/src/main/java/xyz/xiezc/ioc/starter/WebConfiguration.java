package xyz.xiezc.ioc.starter;

import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Configuration;

@Configuration
@BeanScan(basePackages = {"xyz.xiezc.web"})
public class WebConfiguration {


}

