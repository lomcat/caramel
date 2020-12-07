/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lomcat.caramel.core.io;

import com.lomcat.caramel.core.assist.AssertAide;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Copied from
 * <a href="https://github.com/spring-projects/spring-framework/blob/v5.3.1/spring-core/src/main/java/org/springframework/core/io/ClassPathResource.java">
 *     org.springframework.core.io.ClassPathResource
 * </a>
 *
 * <p>
 *     类路径 {@link Resource} 实现。使用指定的 {@link ClassLoader} 或 {@link  Class} 来加载资源。
 * </p>
 *
 * <p>
 *     类路径资源支持解析为 URL。此外如果是位于文件系统中，而不是 JAR 中的资源，也支持将其解析为 {@link java.io.File}。
 * </p>
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 0.0.1
 * @see ClassLoader#getResourceAsStream(String)
 * @see Class#getResourceAsStream(String)
 */
public class ClassPathResource extends AbstractFileResolvingResource {

    private final String path;
    private ClassLoader classLoader;
    private Class<?> clazz;

    /**
     * 使用 {@code ClassLoader} 创建一个新的 {@code ClassPathResource}，
     * 路径（path）参数中的前导斜杠将被删除，因为 ClassLoader 的资源访问方法不接受前导斜杠。
     *
     * <p>这里的 ClassLoader 是线程上下文类加载器。
     *
     * @param path classpath 下的绝对路径
     * @see java.lang.ClassLoader#getResourceAsStream(String)
     * @see ClassUtils#getDefaultClassLoader()
     */
    public ClassPathResource(String path) {
        this(path, (ClassLoader) null);
    }

    /**
     * 使用 {@code ClassLoader} 创建一个新的 {@code ClassPathResource}，
     * 路径（path）参数中的前导斜杠将被删除，因为 ClassLoader 的资源访问方法不接受前导斜杠。
     *
     * @param path classpath 下的绝对路径
     * @param classLoader 用于加载资源的类加载器，若为 {@code null} 则使用线程上下文类加载器
     * @see ClassLoader#getResourceAsStream(String)
     */
    public ClassPathResource(String path, ClassLoader classLoader) {
        AssertAide.notNull(path, "Path must not be null");
        String pathToUse = ResourceUtils.normalizePath(path);
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        this.path = pathToUse;
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    /**
     * 使用 {@link Class} 创建一个新的 {@link ClassPathResource}，
     * 路径（path）参数可以相对于指定的类（clazz），也可以是绝对路径，取决于其是否以斜杠开头。
     *
     * @param path classpath 下的相对或绝对路径
     * @param clazz 用于加载资源的类
     * @see java.lang.Class#getResourceAsStream
     */
    public ClassPathResource(String path, Class<?> clazz) {
        AssertAide.notNull(path, "Path must not be null");
        this.path = ResourceUtils.normalizePath(path);
        this.clazz = clazz;
    }

    /**
     * 返回此资源的路径（作为类路径中的资源）。
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * 返回用于获取此资源的 ClassLoader。
     */
    public final ClassLoader getClassLoader() {
        return (this.clazz != null ? this.clazz.getClassLoader() : this.classLoader);
    }


    /**
     * 此实现检查资源的 URL。
     *
     * @see java.lang.ClassLoader#getResource(String)
     * @see java.lang.Class#getResource(String)
     */
    @Override
    public boolean exists() {
        return (resolveURL() != null);
    }

    /**
     * 将当前类路径资源解析为 URL。
     *
     * @return the resolved URL, or {@code null} if not resolvable
     */
    protected URL resolveURL() {
        if (this.clazz != null) {
            return this.clazz.getResource(this.path);
        } else if (this.classLoader != null) {
            return this.classLoader.getResource(this.path);
        } else {
            return ClassLoader.getSystemResource(this.path);
        }
    }

    /**
     * 此实现打开当前类路径资源的 InputStream
     *
     * @see java.lang.ClassLoader#getResourceAsStream(String)
     * @see java.lang.Class#getResourceAsStream(String)
     */
    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is;
        if (this.clazz != null) {
            is = this.clazz.getResourceAsStream(this.path);
        } else if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        } else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        if (is == null) {
            throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
        }
        return is;
    }

    /**
     * 此实现返回底层类路径资源的 URL（如果可用）。
     *
     * @see java.lang.ClassLoader#getResource(String)
     * @see java.lang.Class#getResource(String)
     */
    @Override
    public URL getURL() throws IOException {
        URL url = resolveURL();
        if (url == null) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
        }
        return url;
    }

    /**
     * 此实现针对当前类路径资源创建一个相对（路径）资源的 ClassPathResource 对象。
     *
     * @see ResourceUtils#applyRelativePath(String, String)
     */
    @Override
    public Resource createRelative(String relativePath) {
        String pathToUse = ResourceUtils.applyRelativePath(this.path, relativePath);
        return this.clazz != null ? new ClassPathResource(pathToUse, this.clazz) : new ClassPathResource(pathToUse, this.classLoader);
    }

    /**
     * 此实现返回该类路径资源指向的文件的名称。
     *
     * @see ResourceUtils#getFilename(String)
     */
    @Override
    public String getFilename() {
        return ResourceUtils.getFilename(this.path);
    }

    /**
     * 此实现返回包含类路径的位置描述
     */
    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder("class path resource [");
        String pathToUse = this.path;
        if (this.clazz != null && !pathToUse.startsWith("/")) {
            builder.append(ClassUtils.classPackageAsResourcePath(this.clazz));
            builder.append('/');
        }
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        builder.append(pathToUse);
        builder.append("]");
        return builder.toString();
    }

    /**
     * 此实现返回底层类路径的哈希码。
     */
    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

}