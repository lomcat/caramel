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

import com.lomcat.caramel.config.listener.ConfigListener;
import com.lomcat.caramel.core.assist.CollectionAide;
import com.lomcat.caramel.core.assist.MapAide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置数据注册器，持有 caramel 配置数据
 *
 * @author Kweny
 * @since 0.0.1
 */
public class ConfigRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ConfigRegistry.class);

    /**
     * 是否启用 Caramel 配置文件加载
     */
    private boolean enabled;
    /**
     * 配置文件内容打印选项
     */
    private CaramelConfigEcho echo;
    /**
     * 是否开启自动刷新（全局，会被每个定位对象的配置覆盖）
     */
    private boolean refreshEnabled;
    private Duration refreshInterval;
    /**
     * 对于配置项的名称是否开启串型和驼峰命名的映射。默认开启
     */
    private boolean mapKebabCamelCase;

    /** 配置资源定位器 */
    private List<ConfigResourceLocator> locators;

    private List<ConfigListener> listeners;

    /** 配置数据注册表，结构为 < key, config > */
    private final Map<String, CaramelConfig> configHolder;

    public ConfigRegistry() {
        this.mapKebabCamelCase = true;
        this.configHolder = new ConcurrentHashMap<>();
    }

    void register(CaramelConfig config) {
        this.configHolder.put(config.getKey(), config);
    }

    /**
     * 获取指定 key 对应的配置数据
     *
     * @param key 配置数据标识，可能是文件名
     * @return 一个 {@link CaramelConfig} 对象
     */
    public CaramelConfig get(String key) {
        return configHolder.get(key);
    }

    /**
     * 获取所有的配置数据
     *
     * @return 一个 key 为配置数据标识，value 为 {@link CaramelConfig} 的 {@link Map} 对象
     */
    public Map<String, CaramelConfig> getAll() {
        return Collections.unmodifiableMap(configHolder);
    }

    public void refresh() {
        // TODO-Kweny refresh
    }

    public void init() {
        if (!this.enabled) {
            logger.debug("[Caramel.Registry] Caramel config is not enabled.");
            return;
        }

        if (CollectionAide.isEmpty(this.locators)) {
            logger.debug("[Caramel.Registry] No caramel config locator.");
        }

        /*
        < 配置数据的key, bunch的集合 > 结构的配置文件集Map，
        每个bunch中可能含有多个文件，这些文件的顺序根据约定优先级从低到高排序（内层），
        每个key可能对应多个bunch，加载之前，会对同key的bunch根据指定优先级从低到高排序（外层），
        因此同key下配置文件资源的加载顺序为：
            for 外层 指定 优先级 从低到高 {
                for 内层 约定 优先级 从低到高
            }
        最终 后加载的高优先级项 将覆盖 先加载的低优先级项
         */

        // 加载配置文件资源集
        Map<String, List<ConfigResourceBunch>> bunchesMap = loadResourceBunches(this.locators);
        if (MapAide.isEmpty(bunchesMap)) {
            logger.debug("[Caramel.Registry] No config resource.");
            return;
        }

        ConfigResourceLoader.create(this, bunchesMap, this.echo, this.listeners).load();

        /*
        TODO-Kweny 重写 Resource 对象，解除对 spring-core.io 的依赖
        TODO-Kweny config 中的配置项名称，驼峰和串型 进行同名覆盖处理（设置一个开启选项） com.fasterxml.jackson.databind.PropertyNamingStrategy
        TODO-Kweny 本地配置文件加载完成，触发监听器（如 开始用云端配置覆盖本地配置 等，同时需要根据 echo 进行打印）
        监听器在 CaramelConfigRegistry 初始化时注册加载，可以考虑 注解扫描 和 代码add 两种方式
        */
    }

    private Map<String, List<ConfigResourceBunch>> loadResourceBunches(List<ConfigResourceLocator> locators) {
        Map<String, List<ConfigResourceBunch>> bunchesMap = new HashMap<>();

        // 根据优先级排序定位器
        Collections.sort(locators);

        for (ConfigResourceLocator locator : locators) {
            // 执行定位
            Map<String, List<ConfigResourceBunch>> locatedBunchesMap = locator.locate();

            if (MapAide.isNotEmpty(locatedBunchesMap)) {
                locatedBunchesMap.forEach((key, bunches) -> {
                    List<ConfigResourceBunch> cachedBunches = bunchesMap.get(key);
                    if (cachedBunches != null) {
                        cachedBunches.addAll(bunches);
                    } else {
                        bunchesMap.put(key, bunches);
                    }
                });
            }
        }

        // TODO-Kweny 通过监听器 加载 远程 bunch
        // 循环本地 bunch ，同时获取远程的同 key bunch，进行优先级合并
        // 循环远程剩余的 bunch（如果还有剩）
        // 定时监听本地和远程，md5不一致则刷新

        return bunchesMap;
    }

    public void destroy() {
        configHolder.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CaramelConfigEcho getEcho() {
        return echo;
    }

    public void setEcho(CaramelConfigEcho echo) {
        this.echo = echo;
    }

    public boolean isRefreshEnabled() {
        return refreshEnabled;
    }

    public void setRefreshEnabled(boolean refreshEnabled) {
        this.refreshEnabled = refreshEnabled;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public boolean isMapKebabCamelCase() {
        return mapKebabCamelCase;
    }

    public void setMapKebabCamelCase(boolean mapKebabCamelCase) {
        this.mapKebabCamelCase = mapKebabCamelCase;
    }

    public List<ConfigListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<ConfigListener> listeners) {
        this.listeners = listeners;
    }

    public void addListener(ConfigListener listener) {
        if (listener == null) {
            return;
        }
        if (this.listeners == null) {
            this.listeners = new ArrayList<>();
        }
        this.listeners.add(listener);
    }

    public List<ConfigResourceLocator> getLocators() {
        return locators;
    }

    public void setLocators(List<ConfigResourceLocator> locators) {
        this.locators = locators;
    }

}
