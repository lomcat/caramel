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

import com.lomcat.caramel.config.internel.PriorityComparable;

import java.util.Map;

/**
 * <p>
 *     配置资源组，包装配置文件资源，携带唯一标识 key 和优先级 priority，实现 Comparable 接口根据 priority 从小到大排序。
 * </p>
 *
 * <p>
 *     每一个 {@link ConfigResourceBunch} 都来源于一个 {@code ”配置定位描述“对象}，
 *     而每个 {@code ”配置定位描述“对象} 由于未指定路径或扩展名等原因，可能会根据约定路径和约定扩展名查找到多个文件资源（Resource），
 *     这些 Resource 在真正被加载为配置数据时，需要根据约定路径和扩展名的优先级进行合并处理。
 * </p>
 *
 * <p>
 *     当每个 {@link ConfigResourceBunch} 中的多个文件资源合并完成后，将作为一个完整的“配置文件组”，其具有 key 和 priority。
 *     之后将同 key 的“配置文件组”根据指定优先级 priority 再次进行合并，
 *     此时一个唯一 key 所标识的完整配置数据 {@link CaramelConfig} 才算加载完成。
 * </p>
 *
 * <p>
 *     <ul>
 *         <li>一个 Bunch 对应的是一个”配置定位描述“对象，如字符串形式的 {@code {redis}(100)/config/redis.conf}，或者一个包含 name、path、extension、key、priority 等属性的对象；</li>
 *         <li>一个 Bunch 中可以包含多个 Resource，如 {@code {redis}(100)redis} 由于未指定路径和扩展名，将根据约定路径和扩展名来查找，可能会找到多个匹配的 Resource；</li>
 *         <li>
 *             一个 Key 可能包含多个 Bunch，如 {@code {redis}(100)/config/redis.conf} 和 {@code {redis}(200)/config/redis-cluster} 两个 Bunch 的 Key 是一样的，前者唯一确定了一个 Resource，而后者由于缺失扩展名可能会查找到多个 Resource。
 *             <ul>
 *                 <li>前者的优先级是 100，后者的优先级是 200，因此后者中的选项将覆盖前者中的同名选项；</li>
 *                 <li>后者中若查找到多个 Resource，这些 Resource 之间的优先级将以具体的定位器实现为准，如对于后者这种本地文件定位来说，会使用约定路径和扩展名的优先级。</li>
 *             </ul>
 *         </li>
 *     </ul>
 * </p>
 *
 * @author Kweny
 * @since 0.0.1
 */
public class ConfigResourceBunch implements PriorityComparable {
    private final String key;
    private final String name;
    private final Double priority;
//    private final List<ConfigResource> resources;
    private final Map<String, ConfigResource> resources;
    private final Boolean refreshEnabled;

    private double softPriority;

    public static ConfigResourceBunch create(String key, String name, Double priority, Map<String, ConfigResource> resources, Boolean refreshEnabled) {
        return new ConfigResourceBunch(key, name, priority, resources, refreshEnabled);
    }

    public ConfigResourceBunch(String key, String name, Double priority, Map<String, ConfigResource> resources, Boolean refreshEnabled) {
        this.key = key;
        this.name = name;
        this.priority = priority;
        this.resources = resources;
        this.refreshEnabled = refreshEnabled;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return name;
    }

    @Override
    public Double getPriority() {
        return this.priority;
    }

    public Map<String, ConfigResource> getResources() {
        return this.resources;
    }

    public Boolean getRefreshEnabled() {
        return this.refreshEnabled;
    }

    double getSoftPriority() {
        return this.softPriority;
    }

    void setSoftPriority(double softPriority) {
        this.softPriority = softPriority;
    }
}