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
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.*;

/**
 * <h3>定位配置文件的位置</h3>
 *
 * @author Kweny
 * @since 0.0.1
 */
class CaramelConfigLocator {

    /** 路径分隔符 */
    private static final String PATH_SEPARATOR = "/";
    /** 文件名和扩展名之间的分隔符 */
    private static final String NAME_SEPARATOR = ".";

    /** 配置文件约定目录，优先级递增，后面的配置将覆盖前面的（若后面配置中某个属性未指定则不会覆盖） */
    private static final String[] DEFAULT_PATHS = {"classpath:/", "classpath:/config/", "file:./", "file:./config/"};
    /** 配置文件约定类型，优先级递增，后面的配置将覆盖前面的（若后面配置中某个属性未指定则不会覆盖） */
    private static final String[] DEFAULT_TYPES = {".properties", ".json", ".conf"};

    static Map<String, List<Resource>> locate(String[] locations, CaramelConfigPosition[] positions) {
        return resolveConfigResources(resolveConfigPositions(locations, positions));
    }

    /**
     * 解析外部配置文件的位置描述符，
     * 将其所描述的文件位置转换为 {@link CaramelConfigPosition} 集合，
     * 需要保证先后顺序。
     */
    private static List<CaramelConfigPosition> resolveConfigPositions(String[] locations, CaramelConfigPosition[] positions) {
        List<CaramelConfigPosition> allPositions = new LinkedList<>();

        // CaramelConfigRegistry#locations
        if (CaramelAide.isNotEmpty(locations)) {
            Arrays.stream(locations).map(CaramelAide::trim).forEach(originLocation -> {
                CaramelConfigPosition position = new CaramelConfigPosition();
                String location = originLocation;

                // 截取路径部分（如果有的话）
                int lastSeparatorIndex = location.lastIndexOf(PATH_SEPARATOR);
                if (lastSeparatorIndex == 0) {
                    position.setPath(PATH_SEPARATOR);
                    location = location.substring(1);
                } else if (lastSeparatorIndex > 0) {
                    position.setPath(location.substring(0, lastSeparatorIndex));
                    location = location.substring(lastSeparatorIndex + 1);
                }

                // 截取扩展名部分（如果有的话）
                int lastPointIndex = location.lastIndexOf(NAME_SEPARATOR);
                if (lastPointIndex > 0) {
                    position.setName(location.substring(0, lastPointIndex));
                    position.setExtension(location.substring(lastPointIndex));
                } else {
                    position.setName(location);
                }

                if (CaramelAide.isBlank(position.getName())) {
                    throw new ConfigLoadException(String.format("[Caramel] Incomplete config file location, a name is required: %s", originLocation));
                }

                position.setKey(position.getName());

                allPositions.add(position);
            });
        }

        // CaramelConfigRegistry#positions
        if (CaramelAide.isNotEmpty(positions)) {
            Arrays.stream(positions).forEach(position -> {
                if (CaramelAide.isBlank(position.getName())) {
                    throw new ConfigLoadException(String.format("[Caramel] Incomplete config file location, a name is required: %s", position));
                }

                if (CaramelAide.isBlank(position.getKey())) {
                    position.setKey(position.getName());
                }

                allPositions.add(position);
            });
        }

        return allPositions;
    }

    /**
     * 根据 {@link CaramelConfigPosition} 集合创建 Spring 的 {@link Resource} 集合，
     * 结构为 < 配置文件Key, Resource列表 >，表示相同 key 的配置有多个文件，
     * 相同 key 的 Resource 列表需保证优先级从低到高的顺序，高优先级文件将覆盖低优先级中的同名属性。
     */
    private static Map<String, List<Resource>> resolveConfigResources(List<CaramelConfigPosition> positions) {
        Map<String, List<Resource>> allResources = new HashMap<>();

        if (CaramelAide.isNotEmpty(positions)) {
            positions.forEach(position -> {
                List<String> paths = new LinkedList<>();
                if (CaramelAide.isNotBlank(position.getPath())) {
                    paths.add(position.getPath());
                } else {
                    // 若未限定配置文件所在根目录，则遍历 DEFAULT_PATHS 中的默认根目录进行查找
                    paths.addAll(Arrays.asList(DEFAULT_PATHS));
                }

                List<String> extensions = new LinkedList<>();
                if (CaramelAide.isNotBlank(position.getExtension())) {
                    extensions.add(position.getExtension());
                } else {
                    // 若未限定配置文件扩展名，则遍历 DEFAULT_TYPES 中的默认扩展名进行查找
                    extensions.addAll(Arrays.asList(DEFAULT_TYPES));
                }

                List<Resource> resources = resolveOneKeyResources(position.getName(), paths, extensions);
                if (CaramelAide.isNotEmpty(resources)) {
                    allResources.put(position.getKey(), resources);
                }
            });
        }

        return allResources;
    }

    /**
     * 多个不同路径下或不同扩展名的配置文件可以使用同一个 key，这些同 key 配置中的属性将根据约定优先级进行覆写，
     * 在此之前需要将这些文件都创建为 Spring 的 {@link Resource} 对象，以便后续加载操作。
     * 注：只创建文件真实存在的 {@link Resource} 对象。
     */
    private static List<Resource> resolveOneKeyResources(String name, List<String> paths, List<String> extensions) {
        List<Resource> resources = new LinkedList<>();

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        paths.forEach(path -> extensions.forEach(extension -> {
            StringBuilder pathBuilder = new StringBuilder();

            pathBuilder.append(path);
            if (!path.endsWith(PATH_SEPARATOR)) {
                pathBuilder.append(PATH_SEPARATOR);
            }

            pathBuilder.append(name);

            if (!extension.startsWith(NAME_SEPARATOR)) {
                pathBuilder.append(NAME_SEPARATOR);
            }
            pathBuilder.append(extension);

            Resource resource = resourceLoader.getResource(pathBuilder.toString());
            if (resource.exists()) {
                resources.add(resource);
            }
        }));

        return resources;
    }
}