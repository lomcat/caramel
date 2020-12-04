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
import com.lomcat.caramel.core.assist.ResourceAide;
import com.lomcat.caramel.core.assist.StringAide;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.*;

/**
 * Modeled on
 * <a href="https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/io/FileSystemResource.java">
 *     org.springframework.core.io.FileSystemResource
 * </a>
 *
 * <p>{@link Resource} 的实现，针对文件系统的 {@link java.io.File} 和 {@link java.nio.file.Path} 句柄。</p>
 *
 * @author Kweny
 * @since 0.0.1
 * @see #FileSystemResource(String)
 * @see #FileSystemResource(File)
 * @see #FileSystemResource(Path)
 * @see java.io.File
 * @see java.nio.file.Files
 */
public class FileSystemResource extends AbstractResource implements WritableResource {

    private final String path;
    private final File file;
    private final Path filePath;

    /**
     * <p>从文件路径创建一个新的 {@link FileSystemResource} 对象。</p>
     * <p>
     *     注意，通过 {@link #createRelative(String)} 构建相对资源时，此处指定的基础路径是否以斜杠结尾会有所不同，
     *     在 "C:/dir1/" 的情况下，相对路径将在该目录下建立，即相对路径 "dir2" -> "C:/dir1/dir2"，
     *     在 "C:/dir1" 的情况下，相对路径将和其同级，即相对路径 "dir2" -> "C:/dir2"。
     * </p>
     *
     * @param path 一个文件路径
     * @see #FileSystemResource(Path)
     */
    public FileSystemResource(String path) {
        AssertAide.notNull(path, "Path must not be null");
        this.path = ResourceAide.normalizePath(path);
        this.file = new File(path);
        this.filePath = this.file.toPath();
    }

    /**
     * <p>从文件句柄创建一个新的 {@link FileSystemResource} 对象。</p>
     * <p>
     *     注意，注意，通过 {@link #createRelative(String)} 构建相对资源时，相对路径将应用于相同的目录级别，
     *     例如，new File("C:/dir1")，其相对路径 "dir2" -> "C:/dir2"，
     *     如果希望在指定目录下构建相对路径，请使用带有文件路径的构造函数 {@link #FileSystemResource(String)}，并在作为构造参数的路径后添加斜杠，如 {@code FileSystemResource("C:/dir1/")}。
     * </p>
     *
     * @param file 一个文件句柄
     * @see #FileSystemResource(Path)
     * @see #getFile()
     */
    public FileSystemResource(File file) {
        AssertAide.notNull(file, "File must not be null");
        this.path = ResourceAide.normalizePath(file.getPath());
        this.file = file;
        this.filePath = file.toPath();
    }

    /**
     * <p>从 {@link Path} 创建一个新的 {@link FileSystemResource} 对象，通过 NIO.2 而非 {@link File} 来执行文件系统交互。</p>
     * <p>
     *     与 {@link PathResource} 相比，此实现严格遵循常规 {@link FileSystemResource} 约定，
     *     尤其是路径规范化和 {@link #createRelative(String)} 处理方面。
     * </p>
     * <p>
     *     注意，注意，通过 {@link #createRelative(String)} 构建相对资源时，相对路径将应用于相同的目录级别，
     *     例如，Paths.get("C:/dir1")，其相对路径 其相对路径 "dir2" -> "C:/dir2"，
     *     如果希望在指定目录下构建相对路径，请使用带有文件路径的构造函数 {@link #FileSystemResource(String)}，并在作为构造参数的路径后添加斜杠，如 {@code FileSystemResource("C:/dir1/")}，
     *     或者考虑在 {@code createRelative} 中将 {@code java.nio.path.Path} 使用 {@link PathResource#PathResource(Path)} 解析。
     * </p>
     *
     * @param filePath 一个文件的 Path 句柄
     * @see #FileSystemResource(File)
     */
    public FileSystemResource(Path filePath) {
        AssertAide.notNull(filePath, "Path must not be null");
        this.path = ResourceAide.normalizePath(filePath.toString());
        this.file = null;
        this.filePath = filePath;
    }

    /**
     * <p>使用 {@link FileSystem} 句柄创建一个新的 {@link FileSystemResource} 对象，并定位到指定路径。</p>
     * <p>这是 {@link #FileSystemResource(String)} 的替代方式，它通过 NIO.2 而非 {@link File} 来执行文件系统交互。</p>
     *
     * @param fileSystem 用于在其中定位路径的文件系统
     * @param path 一个文件路径
     * @see #FileSystemResource(File)
     */
    public FileSystemResource(FileSystem fileSystem, String path) {
        AssertAide.notNull(fileSystem, "FileSystem must not be null");
        AssertAide.notNull(path, "Path must not be null");
        this.path = ResourceAide.normalizePath(path);
        this.file = null;
        this.filePath = fileSystem.getPath(this.path).normalize();
    }

    public final String getPath() {
        return this.path;
    }

    /**
     * 此实现检查底层文件是否存在。
     *
     * @see java.io.File#exists()
     */
    @Override
    public boolean exists() {
        return (this.file != null ? this.file.exists() : Files.exists(this.filePath));
    }

    /**
     * 此实现检查底层文件是否被标记为可读，并且是带有内容的实际文件，而不是目录。
     *
     * @see java.io.File#canRead()
     * @see java.io.File#isDirectory()
     */
    @Override
    public boolean isReadable() {
        return this.file != null ? (this.file.canRead() && !this.file.isDirectory())
                : (Files.isReadable(this.filePath) && !Files.isDirectory(this.filePath));
    }

    /**
     * 此实现为底层文件打开一个 NIO 文件流。
     *
     * @see java.io.FileInputStream
     */
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return Files.newInputStream(this.filePath);
        } catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    /**
     * 此实现检查底层文件是否被标记为可写，并且是带有内容的实际文件，而不是目录。
     *
     * @see java.io.File#canWrite()
     * @see java.io.File#isDirectory()
     */
    @Override
    public boolean isWritable() {
        return this.file != null ? (this.file.canWrite() && !this.file.isDirectory())
                : (Files.isWritable(this.filePath) && !Files.isDirectory(this.filePath));
    }

    /**
     * 此实现为底层文件打开一个 FileOutputStream。
     *
     * @see java.io.FileOutputStream
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(this.filePath);
    }

    /**
     * 此实现返回底层文件的 URL。
     *
     * @see java.io.File#toURI()
     */
    @Override
    public URL getURL() throws IOException {
        return this.file != null ? this.file.toURI().toURL() : this.filePath.toUri().toURL();
    }

    /**
     * 此实现返回底层文件的 URI。
     *
     * @see java.io.File#toURI()
     */
    @Override
    public URI getURI() {
        return this.file != null ? this.file.toURI() : this.filePath.toUri();
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
    public File getFile() {
        return this.file != null ? this.file : this.filePath.toFile();
    }

    /**
     * 此实现为底层文件打开一个 FileChannel。
     *
     * @see java.nio.channels.FileChannel
     */
    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        try {
            return FileChannel.open(this.filePath, StandardOpenOption.READ);
        } catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    /**
     * 此实现为底层文件打开一个 FileChannel。
     *
     * @see java.nio.channels.FileChannel
     */
    @Override
    public WritableByteChannel writableChannel() throws IOException {
        return FileChannel.open(this.filePath, StandardOpenOption.WRITE);
    }

    /**
     * 此实现返回底层 File/Path 的大小.
     */
    @Override
    public long contentLength() throws IOException {
        if (this.file != null) {
            long length = this.file.length();
            if (length == 0L && !this.file.exists()) {
                throw new FileNotFoundException(getDescription() + " cannot be resolved in the file system for checking its content length");
            }
            return length;
        } else {
            try {
                return Files.size(this.filePath);
            } catch (NoSuchFileException ex) {
                throw new FileNotFoundException(ex.getMessage());
            }
        }
    }

    /**
     * 此实现返回底层 File/Path 的最后修改时间.
     */
    @Override
    public long lastModified() throws IOException {
        if (this.file != null) {
            return super.lastModified();
        } else {
            try {
                return Files.getLastModifiedTime(this.filePath).toMillis();
            } catch (NoSuchFileException ex) {
                throw new FileNotFoundException(ex.getMessage());
            }
        }
    }

    /**
     * 此实现针对当前资源创建一个相对（路径）资源的 FileSystemResource 对象。
     *
     * @see ResourceAide#applyRelativePath(String, String)
     */
    @Override
    public Resource createRelative(String relativePath) {
        String pathToUse = ResourceAide.applyRelativePath(this.path, relativePath);
        return this.file != null ? new FileSystemResource(pathToUse)
                : new FileSystemResource(this.filePath.getFileSystem(), pathToUse);
    }

    /**
     * 此实现返回文件的名称。
     *
     * @see java.io.File#getName()
     */
    @Override
    public String getFilename() {
        return this.file != null ? this.file.getName() : this.filePath.getFileName().toString();
    }

    /**
     * 此实现返回包含文件绝对路径的描述。
     *
     * @see java.io.File#getAbsolutePath()
     */
    @Override
    public String getDescription() {
        return "file [" + (this.file != null ? this.file.getAbsolutePath() : this.filePath.toAbsolutePath()) + "]";
    }

    /**
     * 此实现比较底层 File 引用。
     */
    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof FileSystemResource) && StringAide.equals(this.path, ((FileSystemResource) other).path);
    }

    /**
     * 此实现返回底层 File 引用的哈希码。
     */
    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

}