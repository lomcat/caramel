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
import com.lomcat.caramel.config.option.CaramelConfigEcho;
import com.lomcat.caramel.config.option.CaramelConfigProperties;
import com.lomcat.caramel.exception.ConfigLoadException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

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
    private final Map<String, CaramelConfig> caramelConfigHolder;

    public CaramelConfigRegistry() {
        this.caramelConfigHolder = new ConcurrentHashMap<>();
    }

    /**
     * 获取指定 key 对应的配置数据
     *
     * @param key 配置数据标识，可能是文件名
     * @return 一个 {@link CaramelConfig} 对象
     */
    public CaramelConfig get(String key) {
        return caramelConfigHolder.get(key);
    }

    public CaramelConfigProperties getProperties() {
        return properties;
    }

    public void setProperties(CaramelConfigProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取所有的配置数据
     *
     * @return 一个 key 为配置数据标识，value 为 {@link CaramelConfig} 的 {@link Map} 对象
     */
    public Map<String, CaramelConfig> getAll() {
        return Collections.unmodifiableMap(caramelConfigHolder);
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

        /*
        < 配置数据的key, bunch的集合 > 结构的配置文件集Map，
        每个bunch中可能含有多个文件，其顺序根据约定优先级从低到高排序，
        每个key可能对应多个bunch，加载之前，会对同key的bunch根据指定优先级从低到高排序，
        因此同key下配置文件资源的加载顺序为：
            for 外层 指定 优先级 从低到高 {
                for 内层 约定 优先级 从低到高
            }
        最终 后加载的高优先级项 将覆盖 先加载的低优先级项
         */
        Map<String, List<ConfigResourceBunch>> bunchesMap = ConfigLocator.locate(properties.getLocations(), properties.getPositions());
        if (CaramelAide.isEmpty(bunchesMap)) {
            logger.debug("[Caramel] No config file.");
            return;
        }

        CaramelConfigEcho echo = properties.getEcho();

        bunchesMap.forEach((key, resourceBunches) -> {

            StringBuilder echoBuilder = new StringBuilder(String.format("[Caramel] Echo config resource loading process for key '%s'...\n", key));
            if (echo.isSummaryEnabled()) {
                // 启用了 echo.summary，构建 summary 日志内容
                echoBuilder.append(String.format("\tLoad CaramelConfig(%s) from local files...\n", key));
            }

            // 根据优先级从小到大排序
            Collections.sort(resourceBunches);

            AtomicReference<Config> keyConfig = new AtomicReference<>();
            resourceBunches.forEach(bunch -> {
                // 加载合并同一个 bunch 中的多个 resource
                bunch.resources().forEach(resource -> {
                    if (echo.isSummaryEnabled()) {
                        // 启用了 echo.summary，构建 summary 日志内容
                        echoBuilder.append(String.format("\t\tCaramelConfig(druid) <- %s\n", resource));
                    }

                    try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
                        Config resourceConfig = ConfigFactory.parseReader(reader);
                        if (keyConfig.get() == null) {
                            if (echo.isTrackEnabled()) {
                                // 启用了 echo.track， 构建 track 日志内容
                                resourceConfig.entrySet().forEach(entry ->
                                        echoBuilder.append(String.format("\t\t\tNew property into CaramelConfig(%s) <- %s=%s\n", bunch.key(), entry.getKey(), entry.getValue().unwrapped())));
                            }

                            keyConfig.set(resourceConfig);
                        } else {
                            resourceConfig.entrySet().forEach(entry -> {
                                if (echo.isTrackEnabled()) {
                                    // 启用了 echo.track， 构建 track 日志内容
                                    if (keyConfig.get().hasPath(entry.getKey())) {
                                        echoBuilder.append(String.format("\t\t\tRenew property into CaramelConfig(%s) <- %s=%s\n", bunch.key(), entry.getKey(), entry.getValue().unwrapped()));
                                    } else {
                                        echoBuilder.append(String.format("\t\t\tNew property into CaramelConfig(%s) <- %s=%s\n", bunch.key(), entry.getKey(), entry.getValue().unwrapped()));
                                    }
                                }

                                keyConfig.set(keyConfig.get().withValue(entry.getKey(), entry.getValue()));
                            });

                        }
                    } catch (Exception e) {
                        throw new ConfigLoadException(String.format("[Caramel] config file read error: %s", resource), e);
                    }
                });
            });

            // 添加配置数据到注册表
            caramelConfigHolder.put(key, new CaramelConfig(key, keyConfig.get()));

            if (echo.isContentEnabled()) {
                // 启用了 echo.content， 构建 content 日志内容
                CaramelConfig caramelConfig = caramelConfigHolder.get(key);
                if (caramelConfig != null) {
                    echoBuilder.append(String.format("\tContent of CaramelConfig(%s):\n", caramelConfig.getKey()));
                    caramelConfig.content().entrySet().forEach(entry -> echoBuilder.append("\t\t").append(entry.getKey()).append("=").append(entry.getValue().unwrapped()).append("\n"));
                }
            }

            if (echo.isEchoEnabled()) {
                // 启用了 echo.summary,track,content 任意一个，输出到日志
                echo.echo(echoBuilder.toString());
            }

        });



        /*
        TODO-Kweny 重写 Resource 对象，解除对 spring-core.io 的依赖
        TODO-Kweny config 中的配置项名称，驼峰和串型 进行同名覆盖处理
        TODO-Kweny 本地配置文件加载完成，触发监听器（如 开始用云端配置覆盖本地配置 等，同时需要根据 echo 进行打印）
        监听器在 CaramelConfigRegistry 初始化时注册加载，可以考虑 注解扫描 和 代码add 两种方式
        */
    }

    public void destroy() {
        caramelConfigHolder.clear();
        properties = null;
    }

}
