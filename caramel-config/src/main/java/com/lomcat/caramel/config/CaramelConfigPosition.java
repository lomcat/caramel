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
 * <h3>caramel 配置文件的位置描述</h3>
 *
 * <p>用于描述要引入的外部配置文件的所在位置。</p>
 *
 * <p>包括文件名、扩展名、所在目录，以及该配置文件在整个 caramel config 上下文中的唯一标识。</p>
 *
 * @author Kweny
 * @since 0.0.1
 */
public class CaramelConfigPosition {
    /**
     * 配置文件的唯一标识，若不指定则使用 {@link #name}
     */
    private String key;
    /**
     * 配置文件的名称
     */
    private String name;
    /**
     * 配置文件的扩展名
     */
    private String extension;
    /**
     * 配置文件的所在目录
     */
    private String path;

    @Override
    public String toString() {
        return "CaramelConfigPosition{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", extension='" + extension + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}