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

import com.typesafe.config.Config;

/**
 * <h3>Caramel 配置数据</h3>
 *
 * @author Kweny
 * @since 0.0.1
 */
public class CaramelConfig {

    private Config config;

    CaramelConfig(Config config) {
        this.config = config;
    }

    void overwrite(Config newConfig) {
        newConfig.entrySet().forEach(entry -> config = config.withValue(entry.getKey(), entry.getValue()));
    }
}