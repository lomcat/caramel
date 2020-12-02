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
import com.lomcat.caramel.assist.CaramelLogger;
import com.lomcat.caramel.config.option.CaramelConfigProperties;
import com.lomcat.caramel.exception.ConfigLoadException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
public class CaramelConfigRegistry {

    private static final CaramelLogger logger = CaramelLogger.getLogger(CaramelConfigRegistry.class);

    private CaramelConfigProperties properties;

    /** 配置数据注册表，结构为 < key, config > */
    private final Map<String, CaramelConfig> configHolder;

    public CaramelConfigRegistry() {
        this.configHolder = new ConcurrentHashMap<>();
    }

    /**
     * 获取指定 key 对应的配置数据
     *
     * @param key 配置文件标识，可能是文件名
     * @return 一个 {@link CaramelConfig} 对象
     */
    public CaramelConfig get(String key) {
        return configHolder.get(key);
    }

    public CaramelConfigProperties getProperties() {
        return properties;
    }

    public void setProperties(CaramelConfigProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取所有的配置文件对象
     *
     * @return 一个 key 为配置文件标识，value 为 {@link CaramelConfig} 的 {@link Map} 对象
     */
    public Map<String, CaramelConfig> getAll() {
        return Collections.unmodifiableMap(configHolder);
    }

    public void init() {
        if (properties == null) {
            logger.debug("[Caramel] Caramel config properties object is null.");
            return;
        }

        if (!properties.isEnabled()) {
            logger.debug("[Caramel] Caramel config is not enabled.");
            return;
        }

        Map<String, List<Resource>> resourceMap = CaramelConfigLocator.locate(properties.getLocations(), properties.getPositions());
        if (CaramelAide.isEmpty(resourceMap)) {
            logger.debug("[Caramel] No config file.");
            return;
        }

        resourceMap.forEach((key, resources) -> resources.forEach(resource -> {
            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
                Config config = ConfigFactory.parseReader(reader);
                CaramelConfig caramelConfig = configHolder.get(key);
                if (caramelConfig == null) {
                    caramelConfig = new CaramelConfig(key, config);
                    configHolder.put(key, caramelConfig);
                } else {
                    caramelConfig.update(config);
                }
            } catch (Exception e) {
                throw new ConfigLoadException(String.format("[Caramel] config file read error: %s", resource), e);
            }
        }));

        /*
        TODO-Kweny 本地配置文件加载完成，触发监听器（如 echo打印、开始用云端配置覆盖本地配置 等）
        监听器在 CaramelConfigRegistry 初始化时注册加载，可以考虑 注解扫描 和 代码add 两种方式
        */
    }

    public void destroy() {
        configHolder.clear();
        properties = null;
    }

}
