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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Modeled on
 * <a href="https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/io/Resource.java">
 *     org.springframework.core.io.Resource
 * </a>
 *
 * <p>
 *     资源描述符的接口，从底层资源的实际类型——如文件或类路径资源——中抽象而来。
 * </p>
 *
 * <p>
 *     如果资源以物理形式存在，则可以打开其 InputStream，
 *     但对于某些特定资源，只能返回其 URL 或 File 句柄，此处的实际行为取决于具体实现。
 * </p>
 *
 * @author Kweny
 * @since 0.0.1
 */
public interface Resource {

    /**
     * <p>
     *     确定此资源是否实际以物理形式存在。
     * </p>
     * <p>
     *     {@code Resource} 句柄的存在仅保证为有效的资源描述符，
     *     而此方法则执行确定的存在性检查。
     * </p>
     */
    boolean exists();

    /**
     * <p>
     *     指示是否可以通过 {@link #getInputStream()} 读取此资源的非空内容。
     * </p>
     * <p>
     *     对于真实物理存在的典型资源将返回 {@code true}，因为此方法隐含了 {@link #exists()} 语义。
     *     注意，即使返回 {@code true} 的情况，在实际进行内容读取时仍然有可能会失败；而返回 {@code false} 则表示无法读取内容。
     * </p>
     */
    default boolean isReadable() {
        return exists();
    }

    /**
     * <p>
     *     指示此资源所代表的句柄是否已打开了流。
     *     如果为 {@code true}，则不能多次读取 InputStream，必须将其读取并关闭以避免资源泄露。
     * </p>
     * <p>
     *     对于典型的资源描述符将返回 {@code false}。
     * </p>
     */
    default boolean isOpen() {
        return false;
    }

    /**
     * <p>
     *     确定此资源是否代表文件系统中的文件。
     *     当值为 {@code true} 时，{@link #getFile()} 将可以成功调用（强烈建议但不保证）。
     * </p>
     * <p>
     *     默认情况下，将返回保守值 {@code false}。
     * </p>
     */
    default boolean isFile() {
        return false;
    }

    /**
     * <p>返回此资源的 URL 句柄。</p>
     *
     * @throws IOException 当此资源无法解析为 URL，即资源不可用作描述符
     */
    URL getURL() throws IOException;

    /**
     * <p>返回此资源的 URI 句柄。</p>
     *
     * @throws IOException 当此资源无法解析为 URI，即资源不可用作描述符
     */
    URI getURI() throws IOException;

    /**
     * <p>返回此资源的文件句柄。</p>
     *
     * @throws java.io.FileNotFoundException 如果资源无法解析为绝对文件路径，即资源在文件系统中不可用
     * @throws IOException 解析或读取失败
     */
    File getFile() throws IOException;

    /**
     * <p>返回底层资源内容的输入流 {@link InputStream}。</p>
     * <p>期望每次调用都创建一个新的流。</p>
     * <p>
     *     当使用 JavaMail 之类的 API 时，此要求特别重要，当创建邮件附件时，这类 API 必须能够多次读取流。
     *     对于这种用力，要求每次 {@code getInputStream()} 调用都返回一个新的流。
     * </p>
     *
     * @return 底层资源的输入流，不能为 null
     * @throws java.io.FileNotFoundException 如果底层资源不存在
     * @throws IOException 如果无法打开资源内容的输入流
     */
    InputStream getInputStream() throws IOException;

    /**
     * <p>返回一个 {@link ReadableByteChannel} 字节通道对象。</p>
     * <p>期望每次调用都创建一个新的通道。</p>
     * <p>默认实现返回的是使用 {@link #getInputStream()} 输入流创建的通道：{@link Channels#newChannel(InputStream)}。</p>
     *
     * @return 底层资源的字节通道，不得为 null
     * @throws java.io.FileNotFoundException 如果底层资源不存在
     * @throws IOException 如果无法打开资源内容的通道
     * @see #getInputStream()
     */
    default ReadableByteChannel readableChannel() throws IOException {
        return Channels.newChannel(getInputStream());
    }

    /**
     * <p>确定此资源的内容长度。</p>
     *
     * @throws IOException 如果无法解析资源（对于文件系统中的文件资源，或其它已知的物理资源类型）
     */
    long contentLength() throws IOException;

    /**
     * <p>确定此资源最后修改的时间戳。</p>
     *
     * @throws IOException 如果无法解析资源（对于文件系统中的文件资源，或其它已知的物理资源类型）
     */
    long lastModified() throws IOException;

    /**
     * <p>创建此资源的相对（路径）资源。</p>
     *
     * @param relativePath 相对路径（相对于此资源）
     * @return 相对资源的资源句柄
     * @throws IOException 如果无法确定相对资源
     */
    Resource createRelative(String relativePath) throws IOException;

    /**
     * <p>确定此资源的文件名，通常为路径的最后一部分，如 my_file.txt。</p>
     * <p>如果此资源没有文件名，则返回 {@code null}。</p>
     */
    String getFilename();

    /**
     * <p>返回此资源的描述，以便在使用该资源时用于错误输出。</p>
     * <p>同时鼓励实现者利用 {@code toString} 方法返回该值。</p>
     *
     * @see Object#toString()
     */
    String getDescription();

}