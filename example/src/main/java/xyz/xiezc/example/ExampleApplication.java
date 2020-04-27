package xyz.xiezc.example;


import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.starter.WebConfiguration;

@Component
public class ExampleApplication {

    public static void main(String[] args) {
        Xioc xioc = Xioc.run(ExampleApplication.class);
        System.out.println();
    }

    @Inject
    TestExample testExample;

}
