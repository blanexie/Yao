package xyz.xiezc.ioc.test;

import lombok.Data;
import xyz.xiezc.ioc.annotation.Bean;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.annotation.Value;

@Data
@Configuration
public class TestA {

    @Inject
    TestC testC;

    @Value("zzz.xxx.ccc")
    String cc;

    @Bean
    public TestB testB() {
        return new TestB();
    }


    public void print() {
        System.out.println("testB value: " + cc);
    }

}
