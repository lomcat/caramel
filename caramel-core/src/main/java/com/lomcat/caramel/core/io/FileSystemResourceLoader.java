/*
 * Copyright 2002-2017 the original author or authors.
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

/**
 * Copied from
 * <a href="https://github.com/spring-projects/spring-framework/blob/v5.3.1/spring-core/src/main/java/org/springframework/core/io/FileSystemResourceLoader.java">
 *     org.springframework.core.io.FileSystemResourceLoader
 * </a>
 *
 * <p>
 *     {@link ResourceLoader} 的实现，将纯路径解析为文件系统资源，而不是类路径资源（后者是 {@link DefaultResourceLoader} 的默认策略）。
 * </p>
 *
 * <p>
 *     注意：即使纯路径以斜杠开头，它也会被解释为相对于当前 VM 工作目录的相对路径。
 *     可以使用显式的 "file:" 前缀来强制执行绝对文件路径。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 0.0.1
 * @see DefaultResourceLoader
 */
public class FileSystemResourceLoader extends DefaultResourceLoader {

    /**
     * 将资源路径解析为文件系统路径。
     *
     * <p>
     *     注意：即使给定路径以斜杠开头，它也会被解释为相对于当前 VM 工作目录的相对路径。
     *     可以使用显式的 "file:" 前缀来强制执行绝对文件路径。
     * </p>
     *
     * @param path 资源的路径
     * @return 对应的资源句柄
     * @see FileSystemResource
     */
    @Override
    protected Resource getResourceByPath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return new FileSystemContextResource(path);
    }


    /**
     * 显式表示上下文相对路径的 {@link FileSystemResource} 变体。
     */
    public static class FileSystemContextResource extends FileSystemResource {

        public FileSystemContextResource(String path) {
            super(path);
        }

        public String getPathWithinContext() {
            return getPath();
        }
    }

}