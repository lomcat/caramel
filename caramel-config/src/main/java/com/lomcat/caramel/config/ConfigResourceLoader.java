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

import java.util.List;
import java.util.Map;

/**
 * 配置文件资源加载器
 *
 * @author Kweny
 * @since 0.0.1
 */
class ConfigResourceLoader {

    static ConfigResourceLoader create(Map<String, List<ConfigResourceBunch>> bunchesMap, CaramelConfigEcho echo) {
        return new ConfigResourceLoader(bunchesMap, echo);
    }

    private final Map<String, List<ConfigResourceBunch>> bunchesMap;

    private final CaramelConfigEcho echo;

    ConfigResourceLoader(Map<String, List<ConfigResourceBunch>> bunchesMap, CaramelConfigEcho echo) {
        this.bunchesMap = bunchesMap;
        this.echo = echo != null ? echo : new CaramelConfigEcho();
    }

    CaramelConfig load() {

        return null;
    }

}
