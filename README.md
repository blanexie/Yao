# Yao 总框架


##  使用示例
> 说明除了下面示例的方式， 还可以使用FactoryBean接口和@Bean注解方法的方式将bean放入容器中

#### 导入maven包
```xml
<!-- 依赖注入的核心包， 提供基本的依赖注入功能  -->
<dependency>
    <groupId>xyz.xiezc</groupId>
    <artifactId>xioc</artifactId>
    <version>1.0</version>
</dependency>
```

```xml
<!--  整合mybatis的包  -->
<dependency>
    <groupId>xyz.xiezc</groupId>
    <artifactId>xorm</artifactId>
    <version>1.0</version>
</dependency>
```


```xml
<!--  整合netty的包, 提供了基本的http能力和websocket支持  -->
<dependency>
    <groupId>xyz.xiezc</groupId>
    <artifactId>xweb</artifactId>
    <version>1.0</version>
</dependency>

```

##### 定义一个启动类
```java

import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.Configuration;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;

@MapperScan("xyz.xiezc.example.web")
@Configuration
public class ExampleApplication {

    public static void main(String[] args) {
        Xioc.run(ExampleApplication.class);
    }
}
```
####  定义一个Controller放入容器中
```java
import cn.hutool.json.JSONUtil;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.ioc.starter.orm.common.example.Example;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.web.entity.WebContext;

import java.util.List;
import java.util.Map;

@Controller("/")
public class TestController {

    @Inject
    AlbumMapper albumMapper;

    @GetMapping("/test.json")
    public String get(String param) {
        WebContext webContext = WebContext.get();
        //
        Example build = Example.of(Album.class)
                .andEqualTo(Album::getId,3537) //支持类似mybatis-plus的lambda的使用方式
                .build();
        List<Album> albums = albumMapper.selectByExample(build);
        //获取session信息
        Map<String, Object> session = webContext.getSession();
        session.put("param", param);
        
        return JSONUtil.toJsonStr( albums);
    }
}
```
#### 定义一个实体类
```java

import lombok.Data;
import xyz.xiezc.ioc.starter.orm.annotation.Column;
import xyz.xiezc.ioc.starter.orm.annotation.Id;
import xyz.xiezc.ioc.starter.orm.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("t_album")
public class Album implements Serializable {
    @Id
    Integer id;
    @Column
    String title;
    @Column
    String publishTime;
    @Column
    String type;
    @Column
    LocalDateTime createTime;
    @Column
    Integer coverId;
    @Column
    Integer see;
}
```
#### 定义一个Mapper接口
```java
import xyz.xiezc.ioc.starter.orm.common.BaseMapper;

public interface AlbumMapper extends BaseMapper<Album> {
}
```
#### 定义一个websocket的controller
```java

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import xyz.xiezc.ioc.starter.web.annotation.WebSockerController;
import xyz.xiezc.ioc.starter.web.netty.websocket.WebSocketFrameHandler;

@WebSockerController("/websocket")
public class WebSocketHandler implements WebSocketFrameHandler {

    @Override
    public WebSocketFrame handleTextWebSocketFrame(TextWebSocketFrame textWebSocketFrame) {
        String text = textWebSocketFrame.text();
        TextWebSocketFrame textWebSocketFrame1 = new TextWebSocketFrame("resp:" + text);
        return textWebSocketFrame1;
    }

    @Override
    public WebSocketFrame handleBinaryWebSocketFrame(BinaryWebSocketFrame binaryWebSocketFrame) {
        return null;
    }
}

```
#### 配置文件
```properties

## 基本配置信息
# JDBC URL，根据不同的数据库，使用相应的JDBC连接字符串
url = jdbc:mysql://127.0.0.1:8306/daily
# 用户名，此处也可以使用 user 代替
username = root
# 密码，此处也可以使用 pass 代替
password = 123456
# JDBC驱动名，可选（Hutool会自动识别）
driver = com.mysql.cj.jdbc.Driver

## 是否启用ssl
xweb.server.ssl.enable = false
### web服务的端口
xweb.server.port=8443
### 静态文件的目录
xweb.static.path=/static



```


![Xioc](./demo.png)



## Xioc 小型的依赖注入框架， 
#### 目前支持的注解详解
* @Component 被注解的类放入容器成为bean，可以注解类和方法。 
* @Configuration 被注解的类放入容器成为bean, 只能注解类。
* @Inject 注解字段，是的字段从容器中注入bean。 
* @Bean 方法注解， 只能配合@Configuration注解通过方法来生产bean
* @BeanScan 类注解， 只能配合@Configuration一起使用，用来标注框架扫描的包路径
* @Init 方法注解，  被注解的方法在所在bean初始化完成后调用， 方法必须无参 
* @Value 字段注解。  将配置中的内容注入到字段中， 类型转换参照Hutool的类型转换工具。 
* @EventListener 类注解， 被注解的方类必须有无参构造方法，且实现ApplicationListener接口， 注解中必须设置处理的时间名称。

#### 目前支持功能列表如下
* 所有的bean都是单例模式
* 基本的扫描注入类，  
* 支持方法注入bean
* 支持注入配置，配置文件使用Hutool的setting
* 支持类似springboot的starter一样的导入包引入对应功能的方法
> 需要导入的starter包必须在xyz.xiezc.ioc.starter包下放上一个@Configuration注解的类。 并且可以配合@BeanScan一起使用
* 支持bean初始化后调用的init方法
* 支持基本的事件处理器
* 支持配置注入

#### 缺陷待完善
* AOP功能需要在被切的类上面增加注解， 但是AOP的意义就在于不用修改被切类从而达到切面的目的



## xweb 整合netty支持web的框架
#### 支持的注解
* @Controller  参考SpringMvc
* @GetMapping  参考SpringMvc
* @PostMapping  参考SpringMvc
* @RequestBody  参考SpringMvc
* @WebSockerController  被注解的类必须实现WebSocketFrameHandler接口， 从而来实现websocket. 这个注解的value值就是websocket的连接的url

#### 说明
* 默认静态文件目录是 `classPath：static/` ，在这个目录下面的文件会作为静态文件。可以在配置文件中指定 `xweb.static.path` 作为静态文件。
* 请求路径只有在controller中找不到后才会进入静态文件目录下查找。
* GET和POST请求只返回application/json格式的内容，不返回其他类型。这样已经够用了， 现在几乎都是前后端分离的项目。

#### 不完美的地方
* 只支持GET和Post请求
* Post请求只默认提供支持的ContentType类型只有 "application/json" ，"multipart/form-data" ， 
"application/x-www-form-urlencoded"和从url中获取参数的Default几种类型，但是提供了扩展接口。 
只要实现HttpMessageConverter接口，并将实现类放入容器中，就可以了。

#### 支持的配置文件的配置
```shell script


## 是否启用ssl
xweb.server.ssl.enable = false
### web服务的端口
xweb.server.port=8443
### 静态文件的目录
xweb.static.path=/static



```


## XORM 整合mybatis方便查询的项目 
#### 支持的注解
* @Column 标注在实体类中， 可以通过value 指定数据库字段的名称。 
* @Id 标注在实体类中， 可以通过value 指定数据库字段的名称。 
* @MapperScan 一定要标注在启动类中， value指定项目中的mybatis的mapper接口的地址
* @Table 标注在实体类上， 标识这个类对应一个数据库表

#### 支持的配置文件的配置 
* mybatis.configLocation 指定自定义的mybatis的配置文件地址
* mybatis.mapperLocations 指定自定义的mapper.xml文件的路径
* mybatis.typeAliasesPackage 参考mybatis-spring
* mybatis.typeHandlersPackage 参考mybatis-spring

#### 数据源的配置
```shell script

## 基本配置信息
# JDBC URL，根据不同的数据库，使用相应的JDBC连接字符串
url = jdbc:mysql://127.0.0.1:8306/daily
# 用户名，此处也可以使用 user 代替
username = root
# 密码，此处也可以使用 pass 代替
password = 123456
# JDBC驱动名，可选（Hutool会自动识别）
driver = com.mysql.cj.jdbc.Driver

```

#### 说明
* 目前没有实现事务的功能， 还不支持事务。
* Mapper接口必须实现xyz.xiezc.ioc.starter.orm.common.BaseMapper接口，BaseMapper接口提供了基本的方便的查询的方法，十分方便。
* 可以不用谢mapper.xml文件。 只有到BaseMapper接口不满足需求的时候可以加上xml文件，
mapper.xml文件的默认路径是classPath:mapper/ 。 也可以通过配置文件mybatis.mapperLocations来指定

 




