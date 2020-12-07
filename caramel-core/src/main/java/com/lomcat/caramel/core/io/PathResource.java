/*
 * Copyright 2002-2019 the original author or authors.
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

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.*;

/**
 * Copied from
 * <a href="https://github.com/spring-projects/spring-framework/blob/v5.3.1/spring-core/src/main/java/org/springframework/core/io/PathResource.java">
 *     org.springframework.core.io.PathResource
 * </a>
 *
 * <p>
 *     针对 {@link java.nio.file.Path} 句柄的 {@link Resource} 实现，通过 {@link Path} API 执行操作和转换。
 *     支持解析为 {@link File} 和 {@link URL}。
 *     同时实现了 {@link WritableResource} 接口。
 * </p>
 *
 * @author Philippe Marschall
 * @author Juergen Hoeller
 * @since 0.0.1
 */
public class PathResource extends AbstractResource implements WritableResource {

    private final Path path;

    /**
     * <p>从 {@link Path} 句柄创建一个新的 {@link PathResource}。</p>
     * <p>
     *     注意，当使用 {@link #createRelative(String)} 构建相对资源时，相对资源将在此目录下构建，
     *     例如，对于 Paths.get("C:/dir1/")，其相对路径 "dir2" -> "C:/dir1/dir2"。
     * </p>
     *
     * @param path 一个 Path 句柄
     */
    public PathResource(Path path) {
        AssertAide.notNull(path, "Path must not be null");
        this.path = path.normalize();
    }


    /**
     * <p>从指定路径创建一个新的 {@link PathResource}。</p>
     * <p>
     *     注意，当使用 {@link #createRelative(String)} 构建相对资源时，相对资源将在此目录下构建，
     *     例如，对于 Paths.get("C:/dir1/")，其相对路径 "dir2" -> "C:/dir1/dir2"。
     * </p>
     *
     * @param path 一个路径
     * @see java.nio.file.Paths#get(String, String...)
     */
    public PathResource(String path) {
        AssertAide.notNull(path, "Path must not be null");
        this.path = Paths.get(path).normalize();
    }

    /**
     * <p>从 {@link URI} 创建一个新的 {@link PathResource}。</p>
     * <p>
     *     注意，当使用 {@link #createRelative(String)} 构建相对资源时，相对资源将在此目录下构建，
     *     例如，对于 Paths.get("C:/dir1/")，其相对路径 "dir2" -> "C:/dir1/dir2"。
     * </p>
     *
     * @param uri 一个路径的 URI
     * @see java.nio.file.Paths#get(URI)
     */
    public PathResource(URI uri) {
        AssertAide.notNull(uri, "URI must not be null");
        this.path = Paths.get(uri).normalize();
    }


    /**
     * 返回此资源的文件路径。
     */
    public final String getPath() {
        return this.path.toString();
    }

    /**
     * 此实现返回底层文件是否存在。
     *
     * @see java.nio.file.Files#exists(Path, java.nio.file.LinkOption...)
     */
    @Override
    public boolean exists() {
        return Files.exists(this.path);
    }

    /**
     * 此实现检查底层文件是否被标记为可读，并且是带有内容的实际文件，而不是目录。
     *
     * @see java.nio.file.Files#isReadable(Path)
     * @see java.nio.file.Files#isDirectory(Path, java.nio.file.LinkOption...)
     */
    @Override
    public boolean isReadable() {
        return Files.isReadable(this.path) && !Files.isDirectory(this.path);
    }

    /**
     * 此实现为底层文件打开一个 InputStream。
     *
     * @see java.nio.file.spi.FileSystemProvider#newInputStream(Path, OpenOption...)
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (!exists()) {
            throw new FileNotFoundException(getPath() + " (no such file or directory)");
        }
        if (Files.isDirectory(this.path)) {
            throw new FileNotFoundException(getPath() + " (is a directory)");
        }
        return Files.newInputStream(this.path);
    }

    /**
     * 此实现检查底层文件是否被标记为可写，并且是带有内容的实际文件，而不是目录。
     *
     * @see java.nio.file.Files#isWritable(Path)
     * @see java.nio.file.Files#isDirectory(Path, java.nio.file.LinkOption...)
     */
    @Override
    public boolean isWritable() {
        return Files.isWritable(this.path) && !Files.isDirectory(this.path);
    }

    /**
     * 此实现为底层文件打开一个 OutputStream。
     *
     * @see java.nio.file.spi.FileSystemProvider#newOutputStream(Path, OpenOption...)
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (Files.isDirectory(this.path)) {
            throw new FileNotFoundException(getPath() + " (is a directory)");
        }
        return Files.newOutputStream(this.path);
    }

    /**
     * 此实现返回底层文件的 URL。
     *
     * @see java.nio.file.Path#toUri()
     * @see java.net.URI#toURL()
     */
    @Override
    public URL getURL() throws IOException {
        return this.path.toUri().toURL();
    }

    /**
     * 此实现返回底层文件的 URI。
     *
     * @see java.nio.file.Path#toUri()
     */
    @Override
    public URI getURI() {
        return this.path.toUri();
    }

    /**
     * 此实现始终表示一个文件。
     */
    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * 此实现返回底层 File 对象引用。
     */
    @Override
    public File getFile() throws IOException {
        try {
            return this.path.toFile();
        } catch (UnsupportedOperationException ex) {
            // 只能将默认文件系统上的路径转换为文件，对于无法转换的情况抛出异常。
            throw new FileNotFoundException(this.path + " cannot be resolved to absolute file path");
        }
    }

    /**
     * 此实现为底层文件打开一个 Channel。
     *
     * @see Files#newByteChannel(Path, OpenOption...)
     */
    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        try {
            return Files.newByteChannel(this.path, StandardOpenOption.READ);
        } catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    /**
     * 此实现为底层文件打开一个 Channel。
     *
     * @see Files#newByteChannel(Path, OpenOption...)
     */
    @Override
    public WritableByteChannel writableChannel() throws IOException {
        return Files.newByteChannel(this.path, StandardOpenOption.WRITE);
    }

    /**
     * 此实现返回底层文件的大小.
     */
    @Override
    public long contentLength() throws IOException {
        return Files.size(this.path);
    }

    /**
     * 此实现返回底层 File 时间戳。
     *
     * @see java.nio.file.Files#getLastModifiedTime(Path, java.nio.file.LinkOption...)
     */
    @Override
    public long lastModified() throws IOException {
        // 这里不能使用超类的方法，因为这里涉及到文件转换，并且只能将默认文件系统上的路径转换为文件。
        return Files.getLastModifiedTime(this.path).toMillis();
    }

    /**
     * 此实现针对当前资源创建一个相对（路径）资源的 PathResource 对象。
     *
     * @see java.nio.file.Path#resolve(String)
     */
    @Override
    public Resource createRelative(String relativePath) {
        return new PathResource(this.path.resolve(relativePath));
    }

    /**
     * 此实现返回文件的名称。
     *
     * @see java.nio.file.Path#getFileName()
     */
    @Override
    public String getFilename() {
        return this.path.getFileName().toString();
    }

    /**
     * 此实现返回资源绝对路径的描述。
     *
     * @see java.nio.file.Path#toAbsolutePath()
     */
    @Override
    public String getDescription() {
        return "path [" + this.path.toAbsolutePath() + "]";
    }


    /**
     * 此实现比较底层 Path 引用。
     */
    @Override
    public boolean equals(Object other) {
        return (this == other
                || other instanceof PathResource && this.path.equals(((PathResource) other).path));
    }

    /**
     * 此实现返回底层 Path 引用的哈希码。
     */
    @Override
    public int hashCode() {
        return this.path.hashCode();
    }
}