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
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.ds.DSFactory;
import com.mysql.cj.util.StringUtils;
import lombok.Data;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.annotation.*;
import xyz.xiezc.ioc.annotation.handler.ComponentAnnotationHandler;
import xyz.xiezc.ioc.common.context.BeanDefinitionContext;
import xyz.xiezc.ioc.common.event.ApplicationEvent;
import xyz.xiezc.ioc.common.event.ApplicationListener;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.enums.BeanStatusEnum;
import xyz.xiezc.ioc.enums.EventNameConstant;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;
import xyz.xiezc.ioc.starter.orm.common.BaseMapper;
import xyz.xiezc.ioc.starter.orm.xml.MapperFactoryBean;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
@Configuration
public class MybatisAutoConfiguration implements ApplicationListener {

    private static Log log = LogFactory.getLog(MybatisAutoConfiguration.class);

    @Inject
    private MybatisProperties properties;

    public static List<String> mapperScan = new ArrayList<>();

    @Inject(requrie = false)
    private Interceptor[] interceptors;

    @Inject(requrie = false)
    private DatabaseIdProvider databaseIdProvider;

    @Init
    public void checkConfigFileExists() {
        if (this.properties.isCheckConfigLocation() && StrUtil.isNotBlank(this.properties.getConfigLocation())) {
            Assert.state(FileUtil.exist(this.properties.getConfigLocation()), "Cannot find config location: "
                    + " (please add config file or check your Mybatis " + "configuration)");
        }
    }


    @Override
    public void doExecute(ApplicationEvent applicationEvent) {
        BeanDefinitionContext beanDefinitionContext = Xioc.getApplicationContext().getBeanDefinitionContext();
        MapperScan annotation = AnnotationUtil.getAnnotation(Xioc.bootClass, MapperScan.class);
        String[] value = annotation.value();
        mapperScan.addAll(CollUtil.newArrayList(value));
        mapperScan.stream()
                .map(mapperPath -> {
                    ResolverUtil resolverUtil = new ResolverUtil();
                    ResolverUtil implementations = resolverUtil.findImplementations(BaseMapper.class, mapperPath);
                    Set<Class<?>> classes = implementations.getClasses();
                    return classes;
                })
                .flatMap(Collection::stream)
                .forEach(clazz -> {
                    ApplicationContextUtil applicationContext = Xioc.getApplicationContext();
                    AnnotationHandler annotationHandler = applicationContext.getClassAnnotationHandler(Component.class);
                    if (annotationHandler == null) {
                        Class<? extends AnnotationHandler> annotatonHandler = ComponentAnnotationHandler.class;
                        annotationHandler = ReflectUtil.newInstanceIfPossible(annotatonHandler);
                        applicationContext.addAnnotationHandler(annotationHandler);
                    }
                    BeanDefinition beanDefinition = annotationHandler.dealBeanAnnotation(clazz.getName(), MapperFactoryBean.class, applicationContext);
                    MapperFactoryBean mapperFactoryBean = new MapperFactoryBean();
                    mapperFactoryBean.setMapperInterface(clazz);
                    beanDefinition.setBean(mapperFactoryBean);
                    beanDefinition.setBeanStatus(BeanStatusEnum.HalfCooked);
                    beanDefinitionContext.addBeanDefinition(clazz.getName(), clazz, beanDefinition);
                });
    }

    /**
     * 数据源的简单获取
     *
     * @return
     */
    @Bean
    public DataSource dataSource() {
        DataSource ds = DSFactory.create(Xioc.getApplicationContext().getSetting()).getDataSource();
        return ds;
    }

    @Bean
    public MapperScannerUtil mapperScannerUtil() {
        MapperScannerUtil mapperScannerUtil = new MapperScannerUtil();
        String[] mapperLocations = properties.getMapperLocations();
        mapperScannerUtil.dealMapperXml(mapperScan, CollUtil.newArrayList(mapperLocations));
        return mapperScannerUtil;
    }


    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, MapperScannerUtil mapperScannerUtil) throws Exception {
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
        if (!ArrayUtil.isEmpty(this.properties.resolveMapperLocations())) {
            factory.setMapperLocations(mapperScannerUtil.getDocumentMapperDefines());
        }

        return factory.getObject();
    }


    @Override
    public int order() {
        return 0;
    }


}
