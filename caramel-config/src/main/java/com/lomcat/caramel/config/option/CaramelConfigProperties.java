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

package com.lomcat.caramel.config.option;

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
     * 配置文件内容打印选项
     */
    private CaramelConfigEcho echo;
    /**
     * 配置文件路径集合，格式为 [$key:][path/]name[.extension]，
     * 其中 $key、path、extension 部分可选，name 部分必须，
     * 运行时会被转换为 {@link CaramelConfigPosition}。
     * $key: 用于为该配置文件指定一个为标识，必须以 $ 号开头，以 : 号结束，若不指定则以 name 为唯一标识，不同配置文件可以指定一样的 key，同 key 的文件内容将合并。
     */
    private String[] locations;
    /**
     * 配置文件位置描述集合
     */
    private CaramelConfigPosition[] positions;

    /**
     * 返回是否启用了 Caramel 配置文件加载
     *
     * @return true-启用；false-禁用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用 Caramel 配置文件加载
     *
     * @param enabled true-启用；false-禁用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 返回配置文件内容打印选项
     *
     * @return 打印选项对象
     */
    public CaramelConfigEcho getEcho() {
        return echo;
    }

    /**
     * 设置配置文件内容打印选项
     *
     * @param echo 打印选项对象
     */
    public void setEcho(CaramelConfigEcho echo) {
        this.echo = echo;
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