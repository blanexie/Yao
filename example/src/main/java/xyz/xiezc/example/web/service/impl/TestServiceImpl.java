package xyz.xiezc.example.web.service.impl;

import xyz.xiezc.example.web.service.TestService;
import xyz.xiezc.ioc.starter.annotation.Component;

@Component
public class TestServiceImpl implements TestService {

    @Override
    public void print() {
        System.out.println("this is TestServiceImpl");
    }
}