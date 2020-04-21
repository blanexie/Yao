package xyz.xiezc.ioc.test;


import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.FactoryBean;

@Component
public class StarterC implements FactoryBean<StarterA> {


    public void print() {
        System.out.println("StarterC StarterCStarterCStarterC");
    }

    @Override
    public StarterA getObject() throws Exception {
        return new StarterA();
    }

    @Override
    public Class<StarterA> getObjectType() {
        return StarterA.class;
    }
}
