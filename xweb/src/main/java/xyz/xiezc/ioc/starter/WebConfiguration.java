package xyz.xiezc.ioc.starter;

import xyz.xiezc.ioc.annotation.BeanScan;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.annotation.Init;
import xyz.xiezc.ioc.common.context.impl.BeanDefinitionContextUtil;
import xyz.xiezc.ioc.definition.BeanDefinition;

import java.util.HashMap;
import java.util.Map;

@Configuration
@BeanScan(basePackages = {"xyz.xiezc.ioc.starter.web"})
public class WebConfiguration {

}

