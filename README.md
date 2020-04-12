# Yao 总框架


## Xioc 小型的依赖注入框架， 
#### 目前支持的注解详解
* @Component 被注解的类放入容器成为bean，可以注解类和方法。 
* @Configuration 被注解的类放入容器成为bean, 只能注解类。
* @Inject 注解字段，是的字段从容器中注入bean。 
* @Bean 方法注解， 配合@Configuration注解通过方法来 生产bean

#### 目前支持功能列表如下
* 所有的bean都是单例模式
* 基本的扫描注入类，  
* 支持方法注入bean
* 支持注入配置，配置文件使用Hutool的setting

#### 后期支持的功能
* 支持类似springboot的starter一样的导入包引入对应功能的方法
* 整合Servlet,使得支持web功能， 使用hutool的Servlet工具
* 整合netty， 使用netty的web功能。 主要的web方向
* 整合mybatis 使得支持数据
* 整合mybatis后需要支持事务，参考spring的方式管理事务
* 整合mybatis-plus, 方便使用mybatis
* 增加InitMethod注解
* 多例模式， 有待商榷
