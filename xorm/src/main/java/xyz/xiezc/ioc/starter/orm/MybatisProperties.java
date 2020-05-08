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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import lombok.Data;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Value;

/**
 * Configuration properties for Mybatis.
 *
 * @author Eddú Meléndez
 * @author Kazuki Shimizu
 */
@Component
@Data
public class MybatisProperties {

    public static final String MYBATIS_PREFIX = "mybatis";

    /**
     * Config file path.
     */
    @Value(MYBATIS_PREFIX + "configLocation")
    private String configLocation;

    /**
     * Location of mybatis mapper files.
     */
    @Value(MYBATIS_PREFIX + "mapperLocations")
    private String[] mapperLocations;

    /**
     * Package to scan domain objects.
     */
    @Value(MYBATIS_PREFIX + "typeAliasesPackage")
    private String typeAliasesPackage;

    /**
     * Package to scan handlers.
     */
    @Value(MYBATIS_PREFIX + "typeHandlersPackage")
    private String typeHandlersPackage;

    /**
     * Check the config file exists.
     */
    private boolean checkConfigLocation = false;

    /**
     * Execution mode for
     */
    private ExecutorType executorType;

    /**
     * A Configuration object for customize default settings. If {@link #configLocation}
     * is specified, this property is not used.
     */
    private Configuration configuration;

    /**
     * @return
     * @since 1.1.0
     */
    public String getConfigLocation() {
        return this.configLocation;
    }

    /**
     * @return
     * @since 1.1.0
     */
    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    @Deprecated
    public String getConfig() {
        return this.configLocation;
    }

    @Deprecated
    public void setConfig(String config) {
        this.configLocation = config;
    }

    public String[] getMapperLocations() {
        return this.mapperLocations;
    }

    public void setMapperLocations(String[] mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public String getTypeHandlersPackage() {
        return this.typeHandlersPackage;
    }

    public void setTypeHandlersPackage(String typeHandlersPackage) {
        this.typeHandlersPackage = typeHandlersPackage;
    }

    public String getTypeAliasesPackage() {
        return this.typeAliasesPackage;
    }

    public void setTypeAliasesPackage(String typeAliasesPackage) {
        this.typeAliasesPackage = typeAliasesPackage;
    }

    public boolean isCheckConfigLocation() {
        return this.checkConfigLocation;
    }

    public void setCheckConfigLocation(boolean checkConfigLocation) {
        this.checkConfigLocation = checkConfigLocation;
    }

    public ExecutorType getExecutorType() {
        return this.executorType;
    }

    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Resource[] resolveMapperLocations() {
        List<Resource> resources = new ArrayList<Resource>();
        if (this.mapperLocations != null) {
            for (String mapperLocation : this.mapperLocations) {
                try {
                    Resource resourceObj = ResourceUtil.getResourceObj(mapperLocation);
                    resources.addAll(Arrays.asList(resourceObj));
                } catch (Exception e) {

                }
            }
        }

        Resource[] mapperLocations = new Resource[resources.size()];
        mapperLocations = resources.toArray(mapperLocations);
        return mapperLocations;
    }
}
