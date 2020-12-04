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
import com.lomcat.caramel.core.assist.StringAide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Modeled on
 * <a href="https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/io/AbstractResource.java">
 *     org.springframework.core.io.AbstractResource
 * </a>
 *
 * <p>
 *     用于实现 {@link Resource} 的便捷基类，预先实现一些通用的典型行为。
 * </p>
 *
 * <p>
 *     {@link #exists()} 方法将检查是否可以打开 File 或 InputStream；
 *     {@link #isOpen()} 方法将始终返回 {@code false}；
 *     {@link #isFile()} 方法将始终返回 {@code false}；
 *     {@link #getURL()} 和 {@link #getURI()} 方法将直接引发异常；
 *     {@link #toString()} 方法将返回 {@link #getDescription()} 的结果。
 * </p>
 *
 * @author Kweny
 * @since 0.0.1
 */
public abstract class AbstractResource implements Resource {

    /**
     * 此实现通过检查文件或输入流是否可以打开的方式，来确定资源的存在性。
     * 适用于目录资源和内容资源。
     */
    @Override
    public boolean exists() {
        // 检查文件存在性：是否可以在文件系统中找到文件
        if (isFile()) {
            try {
                return getFile().exists();
            } catch (IOException ex) {
                Logger logger = LoggerFactory.getLogger(getClass());
                logger.debug("Could not retrieve File for existence check of " + getDescription(), ex);
            }
        }

        // 后备方式 - 检查流的存在性：是否可以打开输入流
        try {
            getInputStream().close();
            return true;
        } catch (Throwable ex) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.debug("Could not retrieve InputStream for existence check of " + getDescription(), ex);
            return false;
        }
    }

    /**
     * 对于存在的资源（{@link #exists()} 为 {@code true}），此实现始终返回 {@code true}。
     */
    @Override
    public boolean isReadable() {
        return exists();
    }

    /**
     * 此实现始终返回 {@code false}。
     */
    @Override
    public boolean isOpen() {
        return false;
    }

    /**
     * 此实现始终返回 {@code false}。
     */
    @Override
    public boolean isFile() {
        return false;
    }

    /**
     * 此实现假定无法将资源解析为 URL，并引发 FileNotFoundException。
     */
    @Override
    public URL getURL() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
    }

    /**
     * 此实现基于 {@link #getURL()} 返回的 URL 来构建 URI。
     */
    @Override
    public URI getURI() throws IOException {
        URL url = getURL();
        try {
            return ResourceAide.toURI(url);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Invalid URI [" + url + "]", ex);
        }
    }

    /**
     * 此实现假定无法将资源解析为绝对文件路径，并引发 FileNotFoundException。
     */
    @Override
    public File getFile() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
    }

    /**
     * <p>此实现实现返回一个使用 {@link #getInputStream()} 输入流创建的通道：{@link Channels#newChannel(InputStream)}。</p>
     * <p>这与 {@link Resource} 的默认方法相同，此处进行了镜像，以便在类层次结构中进行有效的 JVM 级分派。</p>
     */
    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        return Channels.newChannel(getInputStream());
    }

    /**
     * <p>此实现读取整个 InputStream 以确定资源内容的长度。</p>
     * <p>
     *     对于自定义子类，强烈建议使用更优化的实现覆盖此方法，
     *     例如检查 File 的长度；或者对于只能读取一次的流可以简单地返回 -1。
     * </p>
     *
     * @see #getInputStream()
     */
    @Override
    public long contentLength() throws IOException {
        try (InputStream is = getInputStream()) {
            long size = 0;
            byte[] buffer = new byte[256];
            int read;
            while ((read = is.read(buffer)) != -1) {
                size += read;
            }
            return size;
        }
    }

    /**
     * 此实现检查底层文件的时间戳（如果可用）。
     *
     * @see #getFileForLastModifiedCheck()
     */
    @Override
    public long lastModified() throws IOException {
        File fileToCheck = getFileForLastModifiedCheck();
        long lastModified = fileToCheck.lastModified();
        if (lastModified == 0L && !fileToCheck.exists()) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved in the file system for checking its last-modified timestamp");
        }
        return lastModified;
    }

    /**
     * 确定要用于时间戳检查的文件，默认实现委托给 {@link #getFile()}。
     *
     * @return 用于时间戳检查的文件（不得为 null）
     * @throws FileNotFoundException 如果无法将资源解析为绝对文件路径，即资源在文件系统中不可用
     * @throws IOException 解析/读取失败
     */
    protected File getFileForLastModifiedCheck() throws IOException {
        return getFile();
    }

    /**
     * 此实现假定无法创建此资源的相对资源，直接引发 FileNotFoundException。
     */
    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
    }

    /**
     * 此实现假定此种资源没有文件名，始终返回 {@code null}。
     */
    @Override
    public String getFilename() {
        return null;
    }

    /**
     * 此实现比较 {@link #getDescription()} 的值。
     *
     * @see #getDescription()
     */
    @Override
    public boolean equals(Object other) {
        return (this == other
                || (other instanceof Resource && StringAide.equals(((Resource) other).getDescription(), getDescription())));
    }

    /**
     * 此实现返回 {@link #getDescription()} 值的哈希码。
     * @see #getDescription()
     */
    @Override
    public int hashCode() {
        return getDescription().hashCode();
    }

    /**
     * 此实现返回资源的描述，即 {@link #getDescription()} 的值。
     *
     * @see #getDescription()
     */
    @Override
    public String toString() {
        return getDescription();
    }

}