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

package com.lomcat.caramel.config.local;

import com.lomcat.caramel.config.ConfigResource;
import com.lomcat.caramel.config.ConfigResourceLocator;
import com.lomcat.caramel.config.ConfigResourceBunch;
import com.lomcat.caramel.config.exception.ConfigLocateException;
import com.lomcat.caramel.core.assist.ArrayAide;
import com.lomcat.caramel.core.assist.CollectionAide;
import com.lomcat.caramel.core.assist.MapAide;
import com.lomcat.caramel.core.assist.StringAide;
import com.lomcat.caramel.core.io.DefaultResourceLoader;
import com.lomcat.caramel.core.io.Resource;
import com.lomcat.caramel.core.io.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本地配置文件定位器，{@link ConfigResourceLocator} 的实现，用于定位程序本地的配置文件。
 *
 * @author Kweny
 * @since 0.0.1
 */
public class LocalConfigResourceLocator implements ConfigResourceLocator {

    private static final Logger logger = LoggerFactory.getLogger(LocalConfigResourceLocator.class);

    /** Key 前缀 */
    private static final String KEY_PREFIX = "{";
    /** Key 后缀 */
    private static final String KEY_SUFFIX = "}";
    /** 优先级前缀 */
    private static final String PRIORITY_PREFIX = "(";
    /** 优先级后缀 */
    private static final String PRIORITY_SUFFIX = ")";
    /** 路径分隔符 */
    private static final String PATH_SEPARATOR = "/";
    /** 文件名和扩展名之间的分隔符 */
    private static final String NAME_SEPARATOR = ".";

    /** 配置文件约定目录，优先级递增，后面的配置将覆盖前面的（若后面配置中某个属性未指定则不会覆盖） */
    private static final String[] DEFAULT_PATHS = {"classpath:/", "classpath:/config/", "file:./", "file:./config/"};
    /** 配置文件约定类型，优先级递增，后面的配置将覆盖前面的（若后面配置中某个属性未指定则不会覆盖） */
    private static final String[] DEFAULT_EXTENSIONS = {"", ".properties", ".json", ".conf"};

    /**
     * 定位器的执行优先级
     */
    private Double priority;
    /**
     * 多个定位描述字符串，描述配置文件所在位置，每个字符串的格式为：{key}(priority)path/name.extension，多个之间以半角逗号 {@code ","} 分隔。
     * 其中 key、priority、path、extension 部分可选，name 部分必须。
     * 运行时会被转换为 {@link LocalConfigPosition}。
     */
    private String[] locations;
    /**
     * 多个定位描述对象，描述配置文件所在位置
     */
    private LocalConfigPosition[] positions;

    /**
     * 执行定位
     *
     * @return 以配置数据 key 为键，以同 key 配置文件资源集合为值的映射
     */
    @Override
    public Map<String, List<ConfigResourceBunch>> locate() {
        return resolveConfigResourceBunches(resolveConfigPositions(locations, positions));
    }

    /**
     * 解析外部配置文件的位置描述符，
     * 将其所描述的文件位置转换为 {@link LocalConfigPosition} 集合。
     */
    private static List<LocalConfigPosition> resolveConfigPositions(String[] locations, LocalConfigPosition[] positions) {
        List<LocalConfigPosition> allPositions = new LinkedList<>();

        // CaramelConfigProperties#locations
        if (ArrayAide.isNotEmpty(locations)) {
            Arrays.stream(locations).map(StringAide::trim).forEach(originLocation -> {
                LocalConfigPosition position = new LocalConfigPosition();
                String location = originLocation;

                // 提取 key 和 priority（如果有的话）
                LocationSegmentPickup pickup = LocationSegmentPickup.newInstance(location).pickup();
                if (StringAide.isNotBlank(pickup.key)) {
                    position.setKey(pickup.key);
                }
                if (pickup.priority != null) {
                    position.setPriority(pickup.priority);
                }
                location = pickup.location;

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

                if (StringAide.isBlank(position.getName())) {
                    throw new ConfigLocateException(String.format("[Caramel] Incomplete location (a name is required): %s", originLocation));
                }

                // 若未指定 key，则以 name 为 key
                if (StringAide.isBlank(position.getKey())) {
                    position.setKey(position.getName());
                }

                allPositions.add(position);
            });
        }

        // CaramelConfigProperties#positions
        if (ArrayAide.isNotEmpty(positions)) {
            Arrays.stream(positions).forEach(position -> {
                if (StringAide.isBlank(position.getName())) {
                    throw new ConfigLocateException(String.format("[Caramel] Incomplete location (a name is required): %s", position));
                }

                // 若未指定 key，则以 name 为 key
                if (StringAide.isBlank(position.getKey())) {
                    position.setKey(position.getName());
                }

                allPositions.add(position);
            });
        }

        return allPositions;
    }

    /**
     * <p>
     *     根据 {@link LocalConfigPosition} 集合创建配置文件集 {@link ConfigResourceBunch} 的集合，
     *     结构为 < 配置数据的key, 配置文件集的集合 >，表示相同 key 的配置来源于多个文件。
     *     使用 Map 结构是为了同 key 的配置一次性处理完，以便 echo 打印时同 key 信息集中在一起输出。
     * </p>
     *
     * <p>
     *     需要保证文件资源在集合中根据优先级从低到高的顺序，高优先级文件将覆盖低优先级中的同名属性。
     *     <ul>
     *         <li>
     *             当未指定路径或扩展名时，将根据约定路径和扩展名查找配置资源，可能找到多个配置文件，需要根据约定的路径和扩展名优先级进行合并。
     *         </li>
     *         <li>
     *             当指定了路径或扩展名时，参数 paths 或 extensions 集合中将只有指定的唯一元素，不存在顺序问题。
     *         </li>
     *         <li>
     *             当未指定路径或扩展名时，参数 paths 或 extensions 集合中的元素将依据常量 {@link #DEFAULT_PATHS} 和 {@link #DEFAULT_EXTENSIONS} 的顺序，可以保证其合并优先级。
     *         </li>
     *     </ul>
     * </p>
     */
    private static Map<String, List<ConfigResourceBunch>> resolveConfigResourceBunches(List<LocalConfigPosition> positions) {
        Map<String, List<ConfigResourceBunch>> bunchesMap = new HashMap<>();

        if (CollectionAide.isNotEmpty(positions)) {
            positions.forEach(position -> {
                List<String> paths = new LinkedList<>();
                if (StringAide.isNotBlank(position.getPath())) {
                    paths.add(position.getPath());
                } else {
                    // 若未限定配置文件所在根目录，则遍历 DEFAULT_PATHS 中的默认根目录进行查找
                    paths.addAll(Arrays.asList(DEFAULT_PATHS));
                }

                List<String> extensions = new LinkedList<>();
                if (StringAide.isNotBlank(position.getExtension())) {
                    extensions.add(position.getExtension());
                } else {
                    // 若未限定配置文件扩展名，则遍历 DEFAULT_EXTENSIONS 中的默认扩展名进行查找
                    extensions.addAll(Arrays.asList(DEFAULT_EXTENSIONS));
                }

                // 根据路径和扩展名查找配置资源
                Map<String, ConfigResource> resources = resolveResources(position.getName(), paths, extensions);
                if (MapAide.isNotEmpty(resources)) {
                    ConfigResourceBunch bunch = ConfigResourceBunch.create(position.getKey(), position.getPriority(), resources, position.getRefreshEnabled());
                    List<ConfigResourceBunch> cachedBunches = bunchesMap.computeIfAbsent(bunch.getKey(), k -> new ArrayList<>());
                    cachedBunches.add(bunch);
                }
            });
        }

        return bunchesMap;
    }


    private static Map<String, ConfigResource> resolveResources(String name, List<String> paths, List<String> extensions) {
        Map<String, ConfigResource> resources = new HashMap<>();

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        AtomicInteger priority = new AtomicInteger(0);
        paths.forEach(path -> extensions.forEach(extension -> {
            StringBuilder pathBuilder = new StringBuilder();

            pathBuilder.append(path);
            if (!path.endsWith(PATH_SEPARATOR)) {
                pathBuilder.append(PATH_SEPARATOR);
            }

            pathBuilder.append(name);

            // 约定扩展名中的 ""（空串，无扩展名）无需 append
            if (!"".equals(extension)) {
                if (!extension.startsWith(NAME_SEPARATOR)) {
                    pathBuilder.append(NAME_SEPARATOR);
                }
                pathBuilder.append(extension);
            }

            Resource resource = resourceLoader.getResource(pathBuilder.toString());
            if (resource.exists()) {
                try {
                    resources.put(resource.getDescription(), ConfigResource.create(resource, (double) priority.incrementAndGet()));
                } catch (NoSuchAlgorithmException | IOException ex) {
                    logger.error(String.format("[Caramel.LocalLocator] Error locating config resource: %s", resource), ex);
                }
            }
        }));

        return resources;
    }

    /** 检查并提取 key 和 priority */
    private static class LocationSegmentPickup {
        private String originalLocation;
        private String location;
        private String key;
        private Double priority;

        private static LocationSegmentPickup newInstance(String location) {
            LocationSegmentPickup instance = new LocationSegmentPickup();
            instance.originalLocation = location;
            instance.location = location;
            return instance;
        }

        private LocationSegmentPickup pickup() {
            int keyPrefixIndex = location.indexOf(KEY_PREFIX);
            int keyPrefixLastIndex = location.indexOf(KEY_PREFIX);
            int keySuffixIndex = location.indexOf(KEY_SUFFIX);
            int keySuffixLastIndex = location.indexOf(KEY_SUFFIX);

            boolean isKeySetup = checkSetup(keyPrefixIndex, keyPrefixLastIndex, keySuffixIndex, keySuffixLastIndex, KEY_PREFIX, KEY_SUFFIX);

            int priorityPrefixIndex = location.indexOf(PRIORITY_PREFIX);
            int priorityPrefixLastIndex = location.lastIndexOf(PRIORITY_PREFIX);
            int prioritySuffixIndex = location.indexOf(PRIORITY_SUFFIX);
            int prioritySuffixLastIndex = location.lastIndexOf(PRIORITY_SUFFIX);

            boolean isPrioritySetup = checkSetup(priorityPrefixIndex, priorityPrefixLastIndex, prioritySuffixIndex, prioritySuffixLastIndex, PRIORITY_PREFIX, PRIORITY_SUFFIX);

            if (!isKeySetup && !isPrioritySetup) {
                return this;
            }

            // 前后缀顺序错乱：(xxx{xxx)、(xxx}xxx)、{xxx(xxx}、{xxx)xxx}
            if (isKeySetup && isPrioritySetup
                && ((keyPrefixIndex > priorityPrefixIndex && keyPrefixIndex < prioritySuffixIndex)
                        || (keySuffixIndex > priorityPrefixIndex && keySuffixIndex < prioritySuffixIndex)
                        || (priorityPrefixIndex > keyPrefixIndex && priorityPrefixIndex < keySuffixIndex)
                        || (prioritySuffixIndex > keyPrefixIndex && prioritySuffixIndex < keySuffixIndex)
                    )) {
                throw new ConfigLocateException(String.format("[Caramel] Malformed location (symbols are out of order): %s", originalLocation));
            }

            if (isKeySetup) {
                String keySegment = originalLocation.substring(keyPrefixIndex, keySuffixIndex + 1);
                String keyString = keySegment.replace(KEY_PREFIX, "").replace(KEY_SUFFIX, "");
                if (StringAide.isNotBlank(keyString)) {
                    // 空串、空白字符等同于未指定 key，将使用 name 作为 key
                    this.key = keyString;
                }
                this.location = StringAide.trim(location.replace(keySegment, ""));
            }

            if (isPrioritySetup) {
                String prioritySegment = originalLocation.substring(priorityPrefixIndex, prioritySuffixIndex + 1);
                String priorityString = prioritySegment.replace(PRIORITY_PREFIX, "").replace(PRIORITY_SUFFIX, "");
                if (StringAide.isNotBlank(priorityString)) {
                    // 空串、单个或多个空白字符等同于未指定优先级，因此对于空串和空白字符不做处理（priority = null）
                    try {
                        this.priority = Double.parseDouble(StringAide.trim(priorityString));
                    } catch (NumberFormatException e) {
                        throw new ConfigLocateException(String.format("[Caramel] Malformed location (priority must be a number): %s", originalLocation), e);
                    }
                }
                this.location = StringAide.trim(location.replace(prioritySegment, ""));
            }

            // location 去除 key 和 priority 部分无内容
            if (StringAide.isBlank(this.location)) {
                throw new ConfigLocateException(String.format("[Caramel] Incomplete location (a name is required): %s", originalLocation));
            }

            return this;
        }

        private boolean checkSetup(int prefixIndex, int prefixLastIndex, int suffixIndex, int suffixLastIndex, String prefix, String suffix) {
            // 未设置
            if (prefixIndex == -1 && suffixIndex == -1) {
                return false;
            }

            // 存在多余的前缀或后缀：{xxx{、(xxx(
            if (prefixIndex != prefixLastIndex) {
                throw new ConfigLocateException(String.format("[Caramel] Malformed location (duplicate symbol '%s'): %s", prefix, originalLocation));
            }
            if (suffixIndex != suffixLastIndex) {
                throw new ConfigLocateException(String.format("[Caramel] Malformed location (duplicate symbol '%s'): %s", suffix, originalLocation));
            }

            // 前后缀写反了：}xxx{、)xxx(
            if (prefixIndex > suffixIndex) {
                throw new ConfigLocateException(String.format("[Caramel] Malformed location ( '%s' and '%s' symbols are reversed): %s", prefix, suffix, originalLocation));
            }

            return true;
        }
    }

    @Override
    public Double getPriority() {
        return priority;
    }

    public void setPriority(Double priority) {
        this.priority = priority;
    }

    public String[] getLocations() {
        return locations;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public LocalConfigPosition[] getPositions() {
        return positions;
    }

    public void setPositions(LocalConfigPosition[] positions) {
        this.positions = positions;
    }
}