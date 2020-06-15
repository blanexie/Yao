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
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.*;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import xyz.xiezc.ioc.starter.ApplicationContextUtil;
import xyz.xiezc.ioc.starter.Xioc;
import xyz.xiezc.ioc.starter.annotation.EventListener;
import xyz.xiezc.ioc.starter.annotation.SystemLoad;
import xyz.xiezc.ioc.starter.common.context.BeanCreateContext;
import xyz.xiezc.ioc.starter.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.starter.common.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.common.enums.BeanStatusEnum;
import xyz.xiezc.ioc.starter.common.enums.BeanTypeEnum;
import xyz.xiezc.ioc.starter.event.ApplicationEvent;
import xyz.xiezc.ioc.starter.event.ApplicationListener;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;
import xyz.xiezc.ioc.starter.orm.bean.SqlSessionFactoryBean;
import xyz.xiezc.ioc.starter.orm.common.BaseMapper;
import xyz.xiezc.ioc.starter.orm.common.SpringBootVFS;
import xyz.xiezc.ioc.starter.orm.xml.DocumentMapperDefine;
import xyz.xiezc.ioc.starter.orm.xml.MapperDefine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static xyz.xiezc.ioc.starter.common.enums.EventNameConstant.loadBeanDefinitions;

/**
 * 事件监听器的 init方法会先于 doExecute方法 被触发
 */
@Data
@SystemLoad
@EventListener(eventName = loadBeanDefinitions)
public class MybatisAutoConfiguration implements ApplicationListener {

    private static Log log = LogFactory.get(MybatisAutoConfiguration.class);

    private MybatisProperties properties;

    private Interceptor[] interceptors;

    private DatabaseIdProvider databaseIdProvider;

    @Override
    public void doExecute(ApplicationEvent applicationEvent) {
        ApplicationContextUtil applicationContext = Xioc.getApplicationContext();
        BeanCreateContext beanCreateContext = applicationContext.getBeanCreateContext();
        BeanDefinitionContext beanDefinitionContext = applicationContext.getBeanDefinitionContext();
        List<BeanDefinition> beanDefinitions = beanDefinitionContext.getBeanDefinitions(Interceptor.class);
        List<Interceptor> collect = beanDefinitions.stream()
                .map(beanDefinition -> {
                    beanCreateContext.createBean(beanDefinition);
                    return (Interceptor) beanDefinition.getBean();
                })
                .collect(Collectors.toList());
        interceptors = ArrayUtil.toArray(collect, Interceptor.class);

        BeanDefinition beanDefinition = beanDefinitionContext.getBeanDefinition(MybatisProperties.class);
        beanCreateContext.createBean(beanDefinition);
        properties = beanDefinition.getBean();

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
        BeanDefinitionContext beanDefinitionContext = applicationContext.getBeanDefinitionContext();
        for (MapperDefine mapperDefine : mapperDefines) {
            Class<?> mapperInterface = mapperDefine.getMapperInterface();
            Object mapper = sqlSessionFactory.openSession(true).getMapper(mapperInterface);
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClass(mapperInterface);
            beanDefinition.setBeanName(mapperInterface.getName());
            beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
            beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
            beanDefinition.setBean(mapper);
            beanDefinitionContext.addBeanDefinition(beanDefinition.getBeanName(), beanDefinition.getBeanClass(), beanDefinition);
        }
    }


    private SqlSessionFactory getSqlSessionFactory(ApplicationContextUtil applicationContext, List<DocumentMapperDefine> documentMapperDefines) {
        //1. 获取数据源
        Setting setting = applicationContext.getPropertiesContext().getSetting();
        DataSource ds = DSFactory.create(setting).getDataSource();
        //2. 根据数据源生成sqlSessionFactory
        SqlSessionFactory sqlSessionFactory = createSqlSessionFactory(ds, documentMapperDefines);
        //3.放入容器中
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(SqlSessionFactory.class);
        beanDefinition.setBeanName(SqlSessionFactory.class.getName());
        beanDefinition.setBeanStatus(BeanStatusEnum.Completed);
        beanDefinition.setBeanTypeEnum(BeanTypeEnum.bean);
        beanDefinition.setBean(sqlSessionFactory);
        applicationContext.getBeanDefinitionContext().addBeanDefinition(beanDefinition.getBeanName(), beanDefinition.getBeanClass(), beanDefinition);
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
            Resource resourceObj =  ResourceUtil.getResourceObj(URLUtil.CLASSPATH_URL_PREFIX+s);
            String file1 =resourceObj.getUrl().getFile();
            List<File> files = FileUtil.loopFiles(file1, file -> file.getName().endsWith(".xml"));
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
                        log.info("扫描到mapper接口，mapperInterface: " + mapperDefine.getMapperInterface()
                                + ", 对应实体类：" + mapperDefine.getEntityClazz().getName()
                                + ", 对应表信息：" + JSONUtil.toJsonStr(mapperDefine.getEntityTableDefine().getTable())
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
        if (StrUtil.isNotBlank(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }
        if (StrUtil.isNotBlank(this.properties.getTypeHandlersPackage())) {
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
