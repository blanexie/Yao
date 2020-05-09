package xyz.xiezc.example;


import cn.hutool.json.JSONUtil;
import xyz.xiezc.example.web.Album;
import xyz.xiezc.example.web.AlbumMapper;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;
import xyz.xiezc.ioc.starter.orm.common.Example;

import java.util.List;

@MapperScan("xyz.xiezc.example.web")
@Component
public class ExampleApplication {

    public static void main(String[] args) {
        Xioc xioc = Xioc.run(ExampleApplication.class);
        BeanDefinition injectBeanDefinition = xioc.getApplicationContextUtil().getInjectBeanDefinition(AlbumMapper.class.getName(), AlbumMapper.class);
        AlbumMapper bean = injectBeanDefinition.getBean();
        Example build = Example.of().createCriteria().andEqualTo(Album::getId, 3500).build();
        List<Album> albums = bean.selectByExample(build);
        System.out.println(JSONUtil.toJsonStr(albums));
    }
}
