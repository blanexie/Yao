package xyz.xiezc.example.web.service.impl;

import xyz.xiezc.example.web.controller.TestController;
import xyz.xiezc.example.web.service.TestService;
import xyz.xiezc.ioc.starter.annotation.Component;
import xyz.xiezc.ioc.starter.annotation.Inject;

@Component
public class TestServiceImpl implements TestService {
    @Inject
    TestController testController;

    @Override
    public void print() {
        System.out.println("this is TestServiceImpl");
    }
}