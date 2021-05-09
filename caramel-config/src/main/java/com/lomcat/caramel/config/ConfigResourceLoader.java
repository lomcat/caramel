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

import com.lomcat.caramel.config.exception.ConfigLoadException;
import com.lomcat.caramel.core.assist.StringAide;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 配置文件资源加载器
 *
 * @author Kweny
 * @since 0.0.1
 */
class ConfigResourceLoader {

    static ConfigResourceLoader create(ConfigRegistry registry, Map<String, List<ConfigResourceBunch>> bunchesMap, CaramelConfigEcho echo) {
        return new ConfigResourceLoader(registry, bunchesMap, echo);
    }

    private final ConfigRegistry registry;
    private final Map<String, List<ConfigResourceBunch>> bunchesMap;
    private final CaramelConfigEcho echo;

    ConfigResourceLoader(ConfigRegistry registry, Map<String, List<ConfigResourceBunch>> bunchesMap, CaramelConfigEcho echo) {
        this.registry = registry;
        this.bunchesMap = bunchesMap;
        this.echo = echo != null ? echo : new CaramelConfigEcho();
    }

    void load() {
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

        bunchesMap.forEach((key, bunches) -> {
            // 根据优先级进行排序 Bunch
            Collections.sort(bunches);

            StringBuilder echoBuilder = new StringBuilder(String.format("[Caramel.ResourceLoader] Echo config resource loading process for key '%s'...\n", key));
            __echo_Summary_LoadFromResources(echoBuilder, key);

            AtomicReference<Config> keyConfig = new AtomicReference<>(); // 当前 Key 的 Config
            bunches.forEach(bunch -> {
                List<ConfigResource> resources = new ArrayList<>(bunch.getResources().values());
                Collections.sort(resources);

                resources.forEach(resource -> {
                    __echo_Summary_Resource(echoBuilder, bunch.getKey(), resource);

                    try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
                        Config resourceConfig = ConfigFactory.parseReader(reader);
                        if (keyConfig.get() == null) {
                            keyConfig.set(resourceConfig);
                            __echo_Track_NewConfig(echoBuilder, bunch.getKey(), resourceConfig);
                        } else {
                            resourceConfig.entrySet().forEach(entry -> {
                                String existedPropertyName = null;
                                Object existedPropertyValue = null;
                                if (this.registry.isMapKebabCamelCase()) {
                                    // 如果否开启了串型和驼峰命名的映射
                                    String newNameForEqual = entry.getKey().replace("-", "").toLowerCase();
                                    for (Map.Entry<String, ConfigValue> existedProperty : keyConfig.get().entrySet()) {
                                        String existedNameForEqual = existedProperty.getKey().replace("-", "").toLowerCase();
                                        if (StringAide.equals(newNameForEqual, existedNameForEqual)) {
                                            existedPropertyName = existedProperty.getKey();
                                            existedPropertyValue = existedProperty.getValue().unwrapped();
                                        }
                                    }
                                    if (existedPropertyName != null) {
                                        // 如果已存在同名配置项，删除旧项并添加新项
                                        keyConfig.set(keyConfig.get().withoutPath(existedPropertyName).withValue(entry.getKey(), entry.getValue()));
                                    } else {
                                        keyConfig.set(keyConfig.get().withValue(entry.getKey(), entry.getValue()));
                                    }
                                } else {
                                    if (keyConfig.get().hasPath(entry.getKey())) {
                                        existedPropertyName = entry.getKey();
                                        existedPropertyValue = keyConfig.get().getValue(existedPropertyName).unwrapped();
                                    }
                                    keyConfig.set(keyConfig.get().withValue(entry.getKey(), entry.getValue()));
                                }
                                __echo_Track_RenewConfig(echoBuilder, bunch.getKey(), entry.getKey(), entry.getValue().unwrapped(), existedPropertyName, existedPropertyValue);
                            });
                        }
                    } catch (Exception e) {
                        throw new ConfigLoadException(String.format("[Caramel.ResourceLoader] Error reading config resource: %s", resource), e);
                    }
                });
            });

            this.registry.register(new CaramelConfig(key, keyConfig.get()));
            __echo_Content(echoBuilder, registry.get(key));

            if (echo.isEchoEnabled()) {
                echo.echo(echoBuilder.toString());
            }
        });
    }

    private void __echo_Summary_LoadFromResources(StringBuilder builder, Object... args) {
        if (echo.isEchoEnabled() && echo.isSummaryEnabled()) {
            // 启用了 echo.summary，构建 summary 日志内容
            builder.append(String.format("\tLoad CaramelConfig(%s) from resources...\n", args));
        }
    }

    private void __echo_Summary_Resource(StringBuilder builder, Object... args) {
        if (echo.isEchoEnabled() && echo.isSummaryEnabled()) {
            // 启用了 echo.summary，构建 summary 日志内容
            builder.append(String.format("\t\tCaramelConfig(%s) << %s\n", args));
        }
    }

    private void __echo_Track_NewConfig(StringBuilder builder, String bunchKey, Config resourceConfig) {
        if (echo.isEchoEnabled() && echo.isTrackEnabled()) {
            // 启用了 echo.track， 构建 track 日志内容
            resourceConfig.entrySet().forEach(entry ->
                    builder.append(String.format("\t\t\tNew property into CaramelConfig(%s) << %s=%s\n", bunchKey, entry.getKey(), entry.getValue().unwrapped())));
        }
    }

    private void __echo_Track_RenewConfig(StringBuilder builder, String bunchKey, String propertyName, Object propertyValue, String oldName, Object oldValue) {
        if (echo.isEchoEnabled() && echo.isTrackEnabled()) {
            // 启用了 echo.track， 构建 track 日志内容
            if (oldName != null) {
                builder.append(String.format("\t\t\tRenew property into CaramelConfig(%s) << %s=%s (Overwritten: %s=%s)\n", bunchKey, propertyName, propertyValue, oldName, oldValue));
            } else {
                builder.append(String.format("\t\t\tNew property into CaramelConfig(%s) << %s=%s\n", bunchKey, propertyName, propertyValue));
            }
        }
    }

    private void __echo_Content(StringBuilder builder, CaramelConfig caramelConfig) {
        if (echo.isEchoEnabled() && echo.isContentEnabled()) {
            // 启用了 echo.content， 构建 content 日志内容
            if (caramelConfig != null) {
                // 排序
                List<String> lines = caramelConfig.content().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue().unwrapped() + "\n").sorted().collect(Collectors.toList());

                builder.append(String.format("\tContent of CaramelConfig(%s):\n", caramelConfig.getKey()));
                lines.forEach(line -> builder.append("\t\t").append(line));
            }
        }
    }

}
