package xyz.xiezc.dubbo.provider.apiImpl;


import org.apache.dubbo.config.annotation.DubboService;
import xyz.xiezc.dubbo.api.GreetingsService;

@DubboService
public class GreetingsServiceImpl implements GreetingsService {
    @Override
    public String sayHi(String name) {
        return "hi, " + name;
    }
}