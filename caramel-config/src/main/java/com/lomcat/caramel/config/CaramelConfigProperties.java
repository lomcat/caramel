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

/**
 * <h3>Caramel 配置文件加载选项</h3>
 *
 * @author Kweny
 * @since 0.0.1
 */
public class CaramelConfigProperties {
    /**
     * 是否启用 Caramel 配置文件加载
     */
    private boolean enabled;
    /**
     * 配置文件路径集合，格式为 [path/]name[.extension]，其中 path 和 extension 部分可选，
     * 运行时会被转换为 {@link CaramelConfigPosition}，并使用 <code>name</code> 作为 key。
     */
    private String[] locations;
    /**
     * 配置文件位置描述集合
     */
    private CaramelConfigPosition[] positions;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 设置配置文件路径集合，格式为 [path/]name[.extension]，其中 path 和 extension 部分可选，
     * 运行时会被转换为 {@link CaramelConfigPosition}，并使用 <code>name</code> 作为 key。
     *
     * @param locations 路径集合
     * @see #setPositions(CaramelConfigPosition[])
     */
    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    /**
     * 获取配置文件路径集合
     *
     * @return 路径集合
     */
    public String[] getLocations() {
        return locations;
    }

    /**
     * 设置配置文件位置描述集合
     *
     * @param positions 配置文件位置描述集合
     * @see #setLocations(String[])
     */
    public void setPositions(CaramelConfigPosition[] positions) {
        this.positions = positions;
    }

    /**
     * 获取配置文件位置描述集合
     *
     * @return 配置文件位置描述集合
     */
    public CaramelConfigPosition[] getPositions() {
        return positions;
    }
}