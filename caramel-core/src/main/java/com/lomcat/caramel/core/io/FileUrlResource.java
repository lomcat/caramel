/*
 * Copyright 2002-2020 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Copied from
 * <a href="https://github.com/spring-projects/spring-framework/blob/v5.3.1/spring-core/src/main/java/org/springframework/core/io/FileUrlResource.java">
 *     org.springframework.core.io.FileUrlResource
 * </a>
 *
 * <p>
 *     {@link UrlResource} 的子类，它假定 URL 是一个文件，并实现 {@link WritableResource} 接口。
 *     该资源变体的 {@link #getFile()} 方法会缓存已解析的 {@link File} 句柄。
 * </p>
 *
 * <p>
 *     该资源变体是 {@link DefaultResourceLoader} 从如 "file:..." 的 URL 位置中解析而来，从而允许将其转换为 {@link WritableResource}。
 *     如果是从 {@link java.io.File} 句柄或 NIO {@link java.nio.file.Path} 直接构造，请考虑使用 {@link FileSystemResource}。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 0.0.1
 */
public class FileUrlResource extends UrlResource implements WritableResource {

    private volatile File file;

    /**
     * 根据给定的 URL 对象创建一个新的 {@code FileUrlResource}。
     *
     * <p>注意，这不会强制将 "file" 作为 URL 协议。如果已知某个协议可以解析为文件，则可以接受该协议。
     *
     * @param url 一个 URL
     * @see ResourceUtils#isFileURL(URL)
     * @see #getFile()
     */
    public FileUrlResource(URL url) {
        super(url);
    }

    /**
     * 使用 URL 协议 "file" 基于给定的文件位置创建一个新的 {@code FileUrlResource}。
     *
     * <p>必要时会对给定位置自动进行编码。
     *
     * @param location 位置（即该协议中的文件路径）
     * @throws MalformedURLException 如果给定的位置不符合 URL 规范
     * @see UrlResource#UrlResource(String, String)
     * @see ResourceUtils#URL_PROTOCOL_FILE
     */
    public FileUrlResource(String location) throws MalformedURLException {
        super(ResourceUtils.URL_PROTOCOL_FILE, location);
    }

    @Override
    public File getFile() throws IOException {
        File file = this.file;
        if (file != null) {
            return file;
        }
        file = super.getFile();
        this.file = file;
        return file;
    }

    @Override
    public boolean isWritable() {
        try {
            File file = getFile();
            return file.canWrite() && !file.isDirectory();
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(getFile().toPath());
    }

    @Override
    public WritableByteChannel writableChannel() throws IOException {
        return FileChannel.open(getFile().toPath(), StandardOpenOption.WRITE);
    }

    @Override
    public Resource createRelative(String relativePath) throws MalformedURLException {
        return new FileUrlResource(createRelativeURL(relativePath));
    }

}