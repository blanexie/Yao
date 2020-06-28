package xyz.xiezc.dubbo.provider.apiImpl;


import xyz.xiezc.dubbo.api.GreetingsService;

public class GreetingsServiceImpl implements GreetingsService {
    @Override
    public String sayHi(String name) {
        return "hi, " + name;
    }
}