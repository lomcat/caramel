/*
 * Copyright 2018-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lomcat.caramel.config;

import com.lomcat.caramel.assist.CaramelAide;
import com.lomcat.caramel.exception.ConfigLoadException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>持有 caramel 配置数据</h3>
 *
 * @author Kweny
 * @since 0.0.1
 */
public class CaramelConfigRegistry implements InitializingBean, DisposableBean {
    /**
     * 配置文件路径集合，格式为 [path/]name[.extension]，其中 path 和 extension 部分可选，
     * 运行时会被转换为 {@link CaramelConfigPosition}，并使用 <code>name</code> 作为 key。
     */
    private String[] locations;
    /**
     * 配置文件位置描述集合
     */
    private CaramelConfigPosition[] positions;

    /** < key, 配置数据 > */
    private final Map<String, CaramelConfig> configHolder;

    public CaramelConfigRegistry() {
        this.configHolder = new ConcurrentHashMap<>();
    }

    /**
     * 获取指定 key 对应的配置文件对象
     *
     * @param key 配置文件标识，可能是文件名
     * @return 一个 {@link CaramelConfig} 对象
     */
    public CaramelConfig get(String key) {
        return configHolder.get(key);
    }

    /**
     * 获取所有的配置文件对象
     *
     * @return 一个 key 为配置文件标识，value 为 {@link CaramelConfig} 的 {@link Map} 对象
     */
    public Map<String, CaramelConfig> getAll() {
        return Collections.unmodifiableMap(configHolder);
    }

    /**
     * 设置配置文件路径集合，格式为 [path/]name[.extension]，其中 path 和 extension 部分可选，
     * 运行时会被转换为 {@link CaramelConfigPosition}，并使用 <code>name</code> 作为 key。
     *
     * @param locations 路径集合
     * @see #setPositions(CaramelConfigPosition[])
     */
    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    /**
     * 设置配置文件位置描述集合
     *
     * @param positions 配置文件位置描述集合
     * @see #setLocations(String[])
     */
    public void setPositions(CaramelConfigPosition[] positions) {
        this.positions = positions;
    }

    /** called by {@link #afterPropertiesSet()} */
    private void load() {
        Map<String, List<Resource>> resourceMap = CaramelConfigLocator.locate(locations, positions);
        if (CaramelAide.isEmpty(resourceMap)) {
            return;
        }

        resourceMap.forEach((key, resources) -> resources.forEach(resource -> {
            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
                Config config = ConfigFactory.parseReader(reader);
                CaramelConfig caramelConfig = configHolder.get(key);
                if (caramelConfig == null) {
                    caramelConfig = new CaramelConfig(config);
                    configHolder.put(key, caramelConfig);
                } else {
                    caramelConfig.overwrite(config);
                }
            } catch (Exception e) {
                throw new ConfigLoadException(String.format("[Caramel] config file read error: %s", resource), e);
            }
        }));
    }

    @Override
    public void afterPropertiesSet() {
        load();
    }

    @Override
    public void destroy() {
        configHolder.clear();
        locations = null;
        positions = null;
    }
}