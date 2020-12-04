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

import com.lomcat.caramel.config.option.CaramelConfigPosition;
import com.lomcat.caramel.core.io.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <p>
 *     包装配置文件资源，携带唯一标识 key 和优先级 priority，实现 Comparable 接口根据 priority 从小到大排序。
 * </p>
 *
 * <p>
 *     每一个 {@link ConfigResourceBunch} 都来源于一个 {@link CaramelConfigPosition}，
 *     而每个 {@link CaramelConfigPosition} 由于未指定路径或扩展名的原因，可能会根据约定路径和约定扩展名查找到多个文件资源，
 *     这些 Resource 在真正被加载为配置数据时，需要根据约定路径和扩展名的优先级进行合并处理。
 * </p>
 *
 * <p>
 *     当每个 {@link ConfigResourceBunch} 中的多个文件资源合并完成后，将作为一个完整的“配置文件集”，其具有 key 和 priority。
 *     之后将同 key 的“配置文件集”根据指定优先级 priority 再次进行合并，
 *     此时一个唯一 key 所标识的完整配置数据 {@link CaramelConfig} 才算加载完成。
 * </p>
 *
 * @author Kweny
 * @since 0.0.1
 */
class ConfigResourceBunch implements Comparable<ConfigResourceBunch> {
    private final String key;
    private final Double priority;
    private final List<Resource> resources;

    static ConfigResourceBunch newInstance(String key, Double priority, List<Resource> resources) {
        return new ConfigResourceBunch(key, priority, resources);
    }

    ConfigResourceBunch(String key, Double priority, List<Resource> resources) {
        this.key = key;
        this.priority = priority;
        this.resources = resources;
    }

    String key() {
        return this.key;
    }

    Double priority() {
        return this.priority;
    }

    List<Resource> resources() {
        return this.resources;
    }

    @Override
    public int compareTo(@NotNull ConfigResourceBunch other) {
        if (this.priority() == null && other.priority() == null) {
            return 0;
        }

        if (this.priority() == null) {
            return -1;
        }

        if (other.priority() == null) {
            return 1;
        }

        return this.priority().equals(other.priority()) ? 0 : (this.priority() > other.priority() ? 1 : -1);
    }
}