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

package com.lomcat.caramel.core.io;

import com.lomcat.caramel.core.assist.ResourceAide;

/**
 * Modeled on
 * <a href="https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/io/ResourceLoader.java">
 *     org.springframework.core.io.ResourceLoader
 * </a>
 *
 * <p>
 *     用于加载资源（如类路径或文件系统资源）的策略接口。
 * </p>
 *
 * @author Kweny
 * @since 0.0.1
 * @see Resource
 */
public interface ResourceLoader {

    /** 类路径的伪 URL 前缀："classpath:" */
    String URL_PREFIX_CLASSPATH = ResourceAide.URL_PREFIX_CLASSPATH;

    /**
     * 返回指定资源位置的 Resource 句柄。
     *
     * <p>该句柄应该是始终可重用的资源描述符，并允许多次 {@link Resource#getInputStream()} 调用。</p>
     *
     * <p>
     *     <ul>
     *         <li>必须支持完全限定的 URL，如 "file:C:/test.dat"。</li>
     *         <li>必须支持 classpath 伪 URL，如 "classpath:test.dat"。</li>
     *         <li>应该支持相对路径，如"WEB-INF/test.dat"（特定实现）。</li>
     *     </ul>
     * </p>
     *
     * <p>注意，资源句柄并不意味着资源存在，需要调用 {@link Resource#exists} 来检查其存在性。
     *
     * @param location 资源位置
     * @return 对应的资源句柄（不为 {@code null}）
     * @see #URL_PREFIX_CLASSPATH
     * @see Resource#exists()
     * @see Resource#getInputStream()
     */
    Resource getResource(String location);

    /**
     * 暴露此 ResourceLoader 使用的 ClassLoader。
     *
     * <p>
     *     需要直接访问 ClassLoader 的客户端可以使用 ResourceLoader 以统一的方式操作，而不是依赖于线程上下文 ClassLoader。
     * </p>
     *
     * @return ClassLoader（即使连系统类加载器都无法访问时才返回 {@code null}）
     * @see com.lomcat.caramel.core.assist.ClassAide#getDefaultClassLoader()
     * @see com.lomcat.caramel.core.assist.ClassAide#forName(String, ClassLoader)
     */
    ClassLoader getClassLoader();

}