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

import java.util.List;
import java.util.Map;

/**
 * 配置文件资源定位器接口
 *
 * @author Kweny
 * @since 0.0.1
 */
public interface ConfigLocator extends PriorityComparable {

    /**
     * 定位器的优先级，当配置文件的定位描述中未指定优先级时，其配置覆盖规则将遵循定位器的优先级。
     * 若未指定定位器优先级，则以定位器的创建顺序为准（不保证）。
     */
    @Override
    default Double getPriority() {
        return null;
    }

    /**
     * 根据定位描述字符串和定位描述对象查找配置文件资源。
     *
     * @return 以配置数据 key 为键，以同 key 配置文件资源集合为值的映射
     */
    Map<String, List<ConfigResourceBunch>> locate();

}