# Yao 总框架


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


#### 后期支持的功能
* 整合Servlet, 使得支持web功能，  使用内嵌Jetty
* 整合netty， 使用netty的web功能。 主要的web方向
* 整合mybatis 使得支持数据
* 整合mybatis后需要支持事务，参考spring的方式管理事务
* 整合mybatis-plus, 方便使用mybatis
* 多例模式， 有待商榷
