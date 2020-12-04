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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * Modeled on
 * <a href="https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/io/WritableResource.java">
 *     org.springframework.core.io.WritableResource
 * </a>
 *
 * <p>支持写入的 {@link Resource} 扩展接口，提供一个 OutputStream 访问器。</p>
 *
 * @author Kweny
 * @since 0.0.1
 */
public interface WritableResource extends Resource {

    /**
     * <p>指示是否可以通过 {@link #getOutputStream()} 向此资源写入内容。</p>
     *
     * <p>
     *     对于典型的资源描述符将返回 {@code true}，但在实际执行写入时仍然以后可能失败，
     *     而当返回 {@code false} 时则表示无法修改此资源内容。
     * </p>
     * @see #getOutputStream()
     * @see #isReadable()
     */
    default boolean isWritable() {
        return true;
    }

    /**
     * <p>返回底层资源的输出流 {@link OutputStream}，以允许（覆盖）写入内容。</p>
     *
     * @throws IOException 如果无法打开资源的输出流
     * @see #getInputStream()
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * <p>返回一个 {@link WritableByteChannel} 字节通道对象。</p>
     * <p>期望每次调用都创建一个新的通道。</p>
     * <p>默认实现返回的是使用 {@link #getOutputStream()} 输出流创建的通道：{@link Channels#newChannel(OutputStream)}。</p>
     *
     * @return 底层资源的字节通道，不得为 null
     * @throws java.io.FileNotFoundException 如果底层资源不存在
     * @throws IOException 如果无法打开资源内容的通道
     * @see #getOutputStream()
     */
    default WritableByteChannel writableChannel() throws IOException {
        return Channels.newChannel(getOutputStream());
    }
}