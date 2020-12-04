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

import com.lomcat.caramel.core.assist.AssertAide;
import com.lomcat.caramel.core.assist.ClassAide;
import com.lomcat.caramel.core.assist.ResourceAide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modeled on
 * <a href="https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/io/DefaultResourceLoader.java">
 *     org.springframework.core.io.DefaultResourceLoader
 * </a>
 *
 * <p>
 *     {@link ResourceLoader} 接口的默认实现。
 *     如果位置值是 URL，则返回 {@link UrlResource}；如果是非 URL 路径或者是 "classpath:" 伪 URL，则返回 {@link ClassPathResource}。
 * </p>
 *
 * @author Kweny
 * @since 0.0.1
 */
public class DefaultResourceLoader implements ResourceLoader {

    private ClassLoader classLoader;

    private final Set<ProtocolResolver> protocolResolvers = new LinkedHashSet<>(4);

    private final Map<Class<?>, Map<Resource, ?>> resourceCaches = new ConcurrentHashMap<>(4);

    /**
     * 创建一个新的 DefaultResourceLoader。
     * 在实际访问资源时，将使用线程上下文类加载器进行 ClassLoader 访问。
     * 为了获得更多的控制，请使用 {@link #DefaultResourceLoader(ClassLoader)} 构造器并传入特定 ClassLoader。
     */
    public DefaultResourceLoader() {
    }

    /**
     * 创建一个新的 DefaultResourceLoader。
     *
     * @param classLoader 用于访问类路径资源的 ClassLoader，或者传入 {@code null} 以使用线程上下文类加载器
     */
    public DefaultResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 指定 ClassLoader 以用其加载类路径资源，或者指定为 {@code null} 以使用线程上下文类加载器来访问资源。
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 返回用于加载类路径资源的 ClassLoader。
     *
     * <p>
     *     将传递给所有通过此 ResourceLoader 创建的 ClassPathResource 对象的构造器。
     * </p>
     *
     * @see ClassPathResource
     */
    @Override
    public ClassLoader getClassLoader() {
        return (this.classLoader != null ? this.classLoader : ClassAide.getDefaultClassLoader());
    }

    /**
     * 向此资源加载器中注册指定的解析程序，从而允许处理其它协议。
     *
     * <p>任何此类解析程序都将在标准解析规则之前执行，因此它可以覆盖任何默认规则。</p>
     *
     * @see #getProtocolResolvers()
     */
    public void addProtocolResolver(ProtocolResolver resolver) {
        AssertAide.notNull(resolver, "ProtocolResolver must not be null");
        this.protocolResolvers.add(resolver);
    }

    /**
     * 返回当前注册的协议解析程序的集合，以进行自省和修改。
     */
    public Collection<ProtocolResolver> getProtocolResolvers() {
        return this.protocolResolvers;
    }

    /**
     * 获取指定值类型的缓存，键为 {@link Resource}。
     *
     * @param valueType 值类型，如 ASM {@code MetadataReader}
     * @return 在 {@code ResourceLoader} 级别共享的缓存 {@link Map}
     */
    @SuppressWarnings("unchecked")
    public <T> Map<Resource, T> getResourceCache(Class<T> valueType) {
        return (Map<Resource, T>) this.resourceCaches.computeIfAbsent(valueType, key -> new ConcurrentHashMap<>());
    }

    /**
     * 清理此资源加载器中的所有资源缓存。
     *
     * @see #getResourceCache
     */
    public void clearResourceCaches() {
        this.resourceCaches.clear();
    }

    @Override
    public Resource getResource(String location) {
        AssertAide.notNull(location, "Location must not be null");

        for (ProtocolResolver protocolResolver : getProtocolResolvers()) {
            Resource resource = protocolResolver.resolve(location, this);
            if (resource != null) {
                return resource;
            }
        }

        if (location.startsWith("/")) {
            return getResourceByPath(location);
        } else if (location.startsWith(URL_PREFIX_CLASSPATH)) {
            return new ClassPathResource(location.substring(URL_PREFIX_CLASSPATH.length()), getClassLoader());
        } else {
            try {
                URL url = new URL(location);
                return ResourceAide.isFileURL(url) ? new FileUrlResource(url) : new UrlResource(url);
            } catch (MalformedURLException ex) {
                // 没有 URL -> 解析为资源路径
                return getResourceByPath(location);
            }
        }
    }

    /**
     * 返回指定路径下资源的 Resource 句柄。
     *
     * <p>默认实现支持类路径位置，但可以被覆盖。</p>
     *
     * @param path 资源的路径
     * @return 对应的资源句柄
     * @see ClassPathResource
     */
    protected Resource getResourceByPath(String path) {
        return new ClassPathContextResource(path, getClassLoader());
    }

    /**
     * 显式表示上下文相对路径的 {@link ClassPathResource} 变体。
     */
    public static class ClassPathContextResource extends ClassPathResource {

        public ClassPathContextResource(String path, ClassLoader classLoader) {
            super(path, classLoader);
        }

        public String getPathWithinContext() {
            return getPath();
        }

        @Override
        public Resource createRelative(String relativePath) {
            String pathToUse = ResourceAide.applyRelativePath(getPath(), relativePath);
            return new ClassPathContextResource(pathToUse, getClassLoader());
        }
    }
}