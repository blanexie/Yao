package xyz.xiezc.example;


import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.starter.WebConfiguration;


public class ExampleApplication {

    public static void main(String[] args) {
        Xioc xioc = Xioc.getSingleton().run(ExampleApplication.class);
        System.out.println();
    }


}
