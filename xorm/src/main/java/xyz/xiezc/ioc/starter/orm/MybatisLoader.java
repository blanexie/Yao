//package xyz.xiezc.ioc.starter.orm;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.log.Log;
//import cn.hutool.log.LogFactory;
//import cn.hutool.setting.Setting;
//import org.apache.ibatis.io.ResolverUtil;
//import org.apache.ibatis.parsing.XPathParser;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.apache.ibatis.session.SqlSessionFactoryBuilder;
//import org.apache.ibatis.session.SqlSessionManager;
//import org.w3c.dom.Document;
//import org.xml.sax.SAXException;
//import xyz.xiezc.ioc.Xioc;
//import xyz.xiezc.ioc.annotation.EventListener;
//import xyz.xiezc.ioc.common.event.ApplicationEvent;
//import xyz.xiezc.ioc.common.event.ApplicationListener;
//import xyz.xiezc.ioc.definition.BeanDefinition;
//import xyz.xiezc.ioc.enums.EventNameConstant;
//import xyz.xiezc.ioc.starter.orm.common.BaseMapper;
//import xyz.xiezc.ioc.starter.orm.xml.DocumentMapperDefine;
//import xyz.xiezc.ioc.starter.orm.xml.MapperDefine;
//import xyz.xiezc.ioc.starter.orm.xml.MyXMLConfigBuilder;
//import xyz.xiezc.ioc.starter.orm.xml.MybatisConfigDefine;
//
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@EventListener(eventName = {EventNameConstant.scanBeanDefinitionMethod})
//public class MybatisLoader implements ApplicationListener {
//
//    Log log = LogFactory.get(MybatisLoader.class);
//
//    public static List<String> mapperScan = new ArrayList<>();
//
//    /**
//     * 处理mapper 接口和mapper.xml配置文件
//     */
//    public List<DocumentMapperDefine> dealMapperXml(List<String> mapperScanPackage) {
//        //获取 xyz.xiezc.mzi.dao 接口(继承BaseMapper)的类
//        Set<Class<?>> baseMapperClazz = mapperScanPackage.stream()
//                .map(mapperPath -> getBaseMapperClazz(mapperPath))
//                .flatMap(Collection::stream).collect(Collectors.toSet());
//
//        //获取mapper文件的路径
//        Set<Path> paths = getMapperXmlPath("mybatis.mappers.xml");
//        Set<Path> paths1 = getMapperXmlPath("mybatis.mappers.xml.config");
//        paths.addAll(paths1);
//        List<DocumentMapperDefine> documentPars = paths.stream()
//                .map(path -> {
//                    try {
//                        return new DocumentMapperDefine(path);
//                    } catch (IOException e) {
//                        log.error(e.getMessage(), e);
//                        return null;
//                    }
//                })
//                .filter(documentMapperDefine -> documentMapperDefine != null)
//                .collect(Collectors.toList());
//
//        List<MapperDefine> mapperDefines = baseMapperClazz.stream()
//                .map(mapperClazz -> new MapperDefine(mapperClazz))
//                .collect(Collectors.toList());
//
//        //找到mapper接口对应的xml文档
//        for (MapperDefine mapperDefine : mapperDefines) {
//            boolean findDoc = false;
//            for (DocumentMapperDefine documentMapperDefine : documentPars) {
//                String nameSpace = documentMapperDefine.getNameSpace();
//                String name = mapperDefine.getMapperInterface().getName();
//                if (Objects.equals(name, nameSpace)) {
//                    documentMapperDefine.setMapperDefine(mapperDefine);
//                    findDoc = true;
//                }
//            }
//            if (!findDoc) {
//                documentPars.add(new DocumentMapperDefine(mapperDefine));
//            }
//        }
//
//        for (DocumentMapperDefine documentMapperDefine : documentPars) {
//            documentMapperDefine.checkDoc();
//        }
//
//        return documentPars;
//    }
//
//
//    private Set<Path> getMapperXmlPath(String key) {
//        Setting setting = Xioc.getApplicationContext().getSetting();
//        String xmlMapperPath = setting.getStr(key, "mapper");
//        String s = xmlMapperPath.replaceAll("\\.", "/");
//        File[] files = FileUtil.file(s)
//                .listFiles(pathname -> pathname.getName().endsWith(".xml"));
//        return CollUtil.newArrayList(files).stream()
//                .map(file -> file.toPath())
//                .collect(Collectors.toSet());
//    }
//
//    private Set<Class<?>> getBaseMapperClazz(String mapperPath) {
//        ResolverUtil resolverUtil = new ResolverUtil();
//        ResolverUtil implementations = resolverUtil.findImplementations(BaseMapper.class, mapperPath);
//        Set<Class<?>> classes = implementations.getClasses();
//        return classes;
//    }
//
//    /**
//     * 获取需要扫描mapper接口的路径
//     *
//     * @return
//     */
//    private List<String> getMapperScanPackage() {
//        String s = Xioc.getApplicationContext().getSetting().get("mybatis.mappers.scan");
//        if (StrUtil.isNotBlank(s)) {
//            mapperScan.add(s);
//        }
//        if (mapperScan.isEmpty()) {
//            throw new RuntimeException("你需要配置mapper的位置, 两种方式, 一种在启动类上加上MapperScan注解,  另一种方式是在配置文件中mybatis.mappers.package ");
//        }
//        return mapperScan;
//    }
//
//    @Override
//    public void doExecute(ApplicationEvent applicationEvent) {
//        try {
//
//
//
//
//
//            //获得配置文件的位置
//            Setting setting = Xioc.getApplicationContext().getSetting();
//            String configPath = setting.getStr("mybatis.config", "mybatis-config.xml");
//            //创建解析配置文件
//            log.info("开始解析处理mybatis的主配置文件: {}", configPath);
//            MybatisConfigDefine mybatisConfigDefine = new MybatisConfigDefine(configPath);
//            mybatisConfigDefine.checkMybatisConfig();
//
//            //解析mapper 文件
//            List<String> mapperScanPackage = getMapperScanPackage();
//            log.info("开始解析处理mapper.xml文件;{}", mapperScanPackage);
//            List<DocumentMapperDefine> documentMapperDefines = this.dealMapperXml(mapperScanPackage);
//            //更正配置文件
//            mybatisConfigDefine.setMapperDefines(documentMapperDefines);
//            mybatisConfigDefine.dealMappers();
//            log.info("更正mybatis的祝配置文件中的mappers的配置完成");
//
//            Document configDocument = mybatisConfigDefine.getConfigDocument();
//            log.info("开始启动mybatis...............");
//            //启动mybatis配置
//
//
//            MyXMLConfigBuilder myXMLConfigBuilder = new MyXMLConfigBuilder(new XPathParser(configDocument), null, null);
//            myXMLConfigBuilder.setDocumentMapperDefines(documentMapperDefines);
//
//            SqlSessionFactory build = new SqlSessionFactoryBuilder().build(myXMLConfigBuilder.parse());
//            SqlSessionManager sqlSession = SqlSessionManager.newInstance(build);
//            sqlSession.startManagedSession(true);
//            for (DocumentMapperDefine documentMapperDefine : documentMapperDefines) {
//                MapperDefine mapperDefine = documentMapperDefine.getMapperDefine();
//                Class<?> mapperInterface = mapperDefine.getMapperInterface();
//                Object mapper = sqlSession.getMapper(mapperInterface);
//                mapperDefine.setMapper(mapper);
//                log.info("{} 放入容器中.......", mapperDefine);
//                BeanDefinition beanDefinition = mapperDefine.toBeanDefinition();
//                Xioc.getApplicationContext().getBeanDefinitionContext()
//                        .addBeanDefinition(beanDefinition.getBeanName(), beanDefinition.getBeanClass(), beanDefinition);
//            }
//        } catch (ParserConfigurationException e) {
//            log.error("mybatis初始化", e);
//        } catch (IOException e) {
//            log.error("mybatis初始化", e);
//        } catch (SAXException e) {
//            log.error("mybatis初始化", e);
//        }
//    }
//
//    @Override
//    public int order() {
//        return 0;
//    }
//
//
//}
