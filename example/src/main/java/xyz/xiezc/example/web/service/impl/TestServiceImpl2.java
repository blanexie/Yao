package xyz.xiezc.example.web.service.impl;

import xyz.xiezc.example.web.service.TestService;
import xyz.xiezc.ioc.starter.annotation.core.Component;

@Component
public class TestServiceImpl2 implements TestService {

    @Override
    public void print() {
        System.out.println("this is TestServiceImpl2");
    }

}
