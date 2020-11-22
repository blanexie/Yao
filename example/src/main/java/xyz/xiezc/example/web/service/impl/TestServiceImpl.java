package xyz.xiezc.example.web.service.impl;

import xyz.xiezc.example.web.controller.TestController;
import xyz.xiezc.example.web.service.TestService;
import xyz.xiezc.ioc.annotation.core.Component;
import xyz.xiezc.ioc.annotation.core.Autowire;

@Component
public class TestServiceImpl implements TestService {
    @Autowire
    TestController testController;

    @Override
    public void print() {
        System.out.println("this is TestServiceImpl");
    }
}