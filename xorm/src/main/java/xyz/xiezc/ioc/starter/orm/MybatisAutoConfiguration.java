/**
 * Copyright 2015-2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.xiezc.ioc.starter.orm;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.json.JSONUtil;
import com.mysql.cj.util.StringUtils;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.*;
import xyz.xiezc.ioc.annotation.EventListener;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.BeanTypeEnum;
import xyz.xiezc.ioc.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;
import xyz.xiezc.ioc.starter.orm.common.BaseMapper;
import xyz.xiezc.ioc.starter.orm.common.SpringBootVFS;
import xyz.xiezc.ioc.starter.orm.bean.SqlSessionFactoryBean;
import xyz.xiezc.ioc.starter.orm.xml.DocumentMapperDefine;
import xyz.xiezc.ioc.starter.orm.xml.MapperDefine;

import javax.sql.DataSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * configuration file is specified as a property, those will be considered,
 * otherwise this auto-configuration will attempt to register mappers based on
 * the interface definitions in or under the root auto-configuration package.
 *
 * @author Eddú Meléndez
 * @author Josh Long
 * @author Kazuki Shimizu
 * @author Eduardo Macarrón
 */
@Data
@EventListener(eventName = {EventNameConstant.loadEventListener})
public class MybatisAutoConfiguration implements ApplicationListener {

    private static Log log = LogFactory.getLog(MybatisAutoConfiguration.class);

    private MybatisProperties properties;

    private Interceptor[] interceptors;

    private DatabaseIdProvider databaseIdProvider;

    @Init
    public void checkConfigFileExists() {
        if (this.properties.isCheckConfigLocation() && StrUtil.isNotBlank(this.properties.getConfigLocation())) {
            Assert.state(FileUtil.exist(this.properties.getConfigLocation()), "Cannot find config location: "
                    + " (please add config file or check your Mybatis " + "configuration)");
        }
    }

    @SneakyThrows
    @Override
    public void doExecute(ApplicationEvent applicationEvent) {
        ApplicationContextUtil applicationContext = Xioc.getApplicationContext();

        BeanDefinition injectBeanDefinition = applicationContext.getInjectBeanDefinition(MybatisProperties.class.getName(), MybatisProperties.class);
        properties = injectBeanDefinition.getBean();

        List<BeanDefinition> beanDefinitions = applicationContext.getBeanDefinitions(Interceptor.class);
        List<Interceptor> collect = beanDefinitions.stream().map(beanDefinition -> (Interceptor) beanDefinition.getBean()).collect(Collectors.toList());
        interceptors = ArrayUtil.toArray(collect, Interceptor.class);

        //1. 扫描mapper接口，获取实体类和对应表格的关系。
        //获取注解中配置的信息
        List<MapperDefine> mapperDefines = getMapperDefines();
        //2. 组装改造后的mapper.xml的文档
        List<DocumentMapperDefine> documentMapperDefines = getDocumentMapperDefines(mapperDefines);
        //3. 生成Configuration和SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory(applicationContext, documentMapperDefines);
        //4. 生成Mapper接口的代理类， 并生成对应的bean放入容器中
        createMapperBean(applicationContext, mapperDefines, sqlSessionFactory);
    }

    private void createMapperBean(ApplicationContextUtil applicationContext, List<MapperDefine> mapperDefines, SqlSessionFactory sqlSessionFactory) {
        for (MapperDefine mapperDefine : mapperDefines) {
            Class<?> mapperInterface = mapperDefine.getMapperInterface();
            Object mapper = sqlSessionFactory.openSession(true).getMapper(mapperInterface);
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClass(mapperInterface);
            beanDefinition.setBeanName(mapperInterface.getName());
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
            beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
            beanDefinition.setBean(mapper);
            applicationContext.addBeanDefinition(beanDefinition.getBeanName(), beanDefinition.getBeanClass(), beanDefinition);
        }
    }

    private SqlSessionFactory getSqlSessionFactory(ApplicationContextUtil applicationContext, List<DocumentMapperDefine> documentMapperDefines) {
        //1. 获取数据源
        DataSource ds = DSFactory.create(Xioc.getApplicationContext().getSetting()).getDataSource();
        //2. 根据数据源生成sqlSessionFactory
        SqlSessionFactory sqlSessionFactory = createSqlSessionFactory(ds, documentMapperDefines);
        //3.放入容器中
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(SqlSessionFactory.class);
        beanDefinition.setBeanName(SqlSessionFactory.class.getName());
        beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        beanDefinition.setBean(sqlSessionFactory);
        applicationContext.addBeanDefinition(beanDefinition.getBeanName(), beanDefinition.getBeanClass(), beanDefinition);
        return sqlSessionFactory;
    }

    private List<DocumentMapperDefine> getDocumentMapperDefines(List<MapperDefine> mapperDefines) {
        List<File> ret = CollUtil.newArrayList();

        List<String> mapperLocations = CollUtil.newArrayList();
        if (properties != null) {
            String[] propertiesMapperLocations = properties.getMapperLocations();
            ArrayList<String> strings = CollUtil.newArrayList(propertiesMapperLocations);
            mapperLocations.addAll(strings);
        }
        for (String mapperLocation : mapperLocations) {
            String s = mapperLocation.replaceAll("\\.", "/");
            List<File> files = FileUtil.loopFiles(s, file -> file.getName().endsWith(".xml"));
            ret.addAll(files);
        }
        List<DocumentMapperDefine> documentPars = ret.stream()
                .map(file -> {
                    try {
                        return new DocumentMapperDefine(file);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(documentMapperDefine -> documentMapperDefine != null)
                .collect(Collectors.toList());

        //找到mapper接口对应的xml文档
        for (MapperDefine mapperDefine : mapperDefines) {
            boolean findDoc = false;
            for (DocumentMapperDefine documentMapperDefine : documentPars) {
                String nameSpace = documentMapperDefine.getNameSpace();
                String name = mapperDefine.getMapperInterface().getName();
                if (Objects.equals(name, nameSpace)) {
                    documentMapperDefine.setMapperDefine(mapperDefine);
                    findDoc = true;
                }
            }
            if (!findDoc) {
                documentPars.add(new DocumentMapperDefine(mapperDefine));
            }
        }
        for (DocumentMapperDefine documentMapperDefine : documentPars) {
            documentMapperDefine.checkDoc();
        }
        return documentPars;
    }

    private List<MapperDefine> getMapperDefines() {
        MapperScan annotation = AnnotationUtil.getAnnotation(Xioc.bootClass, MapperScan.class);
        String[] value = annotation.value();
        List<MapperDefine> ret = CollUtil.newArrayList();
        for (String mapperPath : value) {
            ResolverUtil resolverUtil = new ResolverUtil();
            ResolverUtil implementations = resolverUtil.findImplementations(BaseMapper.class, mapperPath);
            Set<Class<?>> classes = implementations.getClasses();
            classes.stream().map(MapperDefine::new)
                    .forEach(mapperDefine -> {
                        log.debug("扫描到mapper接口，mapperInterface: " + mapperDefine.getMapperInterface()
                                + ", 对应实体类：" + mapperDefine.getEntityClazz().getName()
                                + ", 对应表信息：" + JSONUtil.toJsonStr(mapperDefine.getEntityTableDefine())
                        );
                        ret.add(mapperDefine);
                    });
        }
        return ret;
    }

    @SneakyThrows
    public SqlSessionFactory createSqlSessionFactory(DataSource dataSource, List<DocumentMapperDefine> documentMapperDefines) {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StrUtil.isNotBlank(this.properties.getConfigLocation())) {
            factory.setConfigLocation(ResourceUtil.getResourceObj(this.properties.getConfigLocation()));
        }
        factory.setConfiguration(properties.getConfiguration());
        if (!ArrayUtil.isEmpty(this.interceptors)) {
            factory.setPlugins(this.interceptors);
        }
        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }
        if (!StringUtils.isNullOrEmpty(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }
        if (!StringUtils.isNullOrEmpty(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }
        if (CollUtil.isNotEmpty(documentMapperDefines)) {
            factory.setDocumentMapperDefines(documentMapperDefines);
        }

        return factory.getObject();
    }


    @Override
    public int order() {
        return 0;
    }


}
