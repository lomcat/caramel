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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Copied from
 * <a href="https://github.com/spring-projects/spring-framework/blob/v5.3.1/spring-core/src/main/java/org/springframework/core/io/VfsResource.java">
 *     org.springframework.core.io.VfsResource
 * </a>
 *
 * <p>基于 JBoss VFS 的 {@link Resource} 实现。</p>
 *
 * @author Ales Justin
 * @author Juergen Hoeller
 * @author Costin Leau
 * @author Sam Brannen
 * @since 0.0.1
 */
public class VfsResource extends AbstractResource {

    private final Object resource;


    /**
     * 使用指定的资源句柄创建一个新的 {@link VfsResource}。
     *
     * @param resource 一个 {@code org.jboss.vfs.VirtualFile} 实例（未指定类型以免对 VFS API 产生静态依赖）
     */
    public VfsResource(Object resource) {
        AssertAide.notNull(resource, "VirtualFile must not be null");
        this.resource = resource;
    }


    @Override
    public InputStream getInputStream() throws IOException {
        return VfsHelper.getInputStream(this.resource);
    }

    @Override
    public boolean exists() {
        return VfsHelper.exists(this.resource);
    }

    @Override
    public boolean isReadable() {
        return VfsHelper.isReadable(this.resource);
    }

    @Override
    public URL getURL() throws IOException {
        try {
            return VfsHelper.getURL(this.resource);
        } catch (Exception ex) {
            throw new IOException("Failed to obtain URL for file " + this.resource, ex);
        }
    }

    @Override
    public URI getURI() throws IOException {
        try {
            return VfsHelper.getURI(this.resource);
        }
        catch (Exception ex) {
            throw new IOException("Failed to obtain URI for " + this.resource, ex);
        }
    }

    @Override
    public File getFile() throws IOException {
        return VfsHelper.getFile(this.resource);
    }

    @Override
    public long contentLength() throws IOException {
        return VfsHelper.getSize(this.resource);
    }

    @Override
    public long lastModified() throws IOException {
        return VfsHelper.getLastModified(this.resource);
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        if (!relativePath.startsWith(".") && relativePath.contains("/")) {
            try {
                return new VfsResource(VfsHelper.getChild(this.resource, relativePath));
            }
            catch (IOException ex) {
                // fall back to getRelative
            }
        }

        return new VfsResource(VfsHelper.getRelative(new URL(getURL(), relativePath)));
    }

    @Override
    public String getFilename() {
        return VfsHelper.getName(this.resource);
    }

    @Override
    public String getDescription() {
        return "VFS resource [" + this.resource + "]";
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof VfsResource &&
                this.resource.equals(((VfsResource) other).resource)));
    }

    @Override
    public int hashCode() {
        return this.resource.hashCode();
    }

}