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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;

/**
 * Modeled on
 * <a href="https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/io/AbstractFileResolvingResource.java">
 *     org.springframework.core.io.AbstractFileResolvingResource
 * </a>
 *
 * <p>
 *     该基类抽象了将 URL 解析为 File 的逻辑，用于 {@link UrlResource} 或 {@link ClassPathResource} 等支持 URL -> File 解析的资源实现。
 * </p>
 *
 * <p>
 *     在 URL 中检测 "file" 协议以及 JBoss 的 "vfs" 协议，从而解析为文件系统中的文件引用。
 * </p>
 *
 * @author Kweny
 * @since 0.0.1
 */
public abstract class AbstractFileResolvingResource extends AbstractResource {

    @Override
    public boolean exists() {
        try {
            URL url = getURL();
            if (ResourceAide.isFileURL(url)) {
                // 文件系统
                return getFile().exists();
            } else {
                // 尝试 URL 连接的 content-length 头
                URLConnection conn = url.openConnection();
                customizeConnection(conn);
                HttpURLConnection httpConn = (conn instanceof  HttpURLConnection) ? (HttpURLConnection) conn : null;
                if (httpConn != null) {
                    int code = httpConn.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        return true;
                    } else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return false;
                    }
                }
                if (conn.getContentLengthLong() > 0) {
                    return true;
                }
                if (httpConn != null) {
                    // 非 HTTP OK 状态，无 content-length 头：放弃
                    httpConn.disconnect();
                    return false;
                } else {
                    getInputStream().close();
                    return true;
                }
            }
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public boolean isReadable() {
        try {
            URL url = getURL();
            if (ResourceAide.isFileURL(url)) {
                // 文件系统
                File file = getFile();
                return file.canRead() && !file.isDirectory();
            } else {
                // 尝试输入流解析以获得 jar 资源
                URLConnection conn = url.openConnection();
                customizeConnection(conn);
                if (conn instanceof HttpURLConnection) {
                    HttpURLConnection httpConn = (HttpURLConnection) conn;
                    int code = httpConn.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK) {
                        httpConn.disconnect();
                        return false;
                    }
                }
                long contentLength = conn.getContentLengthLong();
                if (contentLength > 0) {
                    return true;
                } else if (contentLength == 0) {
                    // 空文件或目录 -> 不可读
                    return false;
                } else {
                    getInputStream().close();
                    return true;
                }
            }
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public boolean isFile() {
        try {
            URL url = getURL();
            if (url.getProtocol().startsWith(ResourceAide.URL_PROTOCOL_VFS)) {
                return VfsResourceDelegate.getResource(url).isFile();
            }
            return ResourceAide.URL_PROTOCOL_FILE.equals(url.getProtocol());
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * 此实现返回底层类路径资源的 File 引用，前提是它指向的是一个文件系统中的文件。
     *
     * @see ResourceAide#getFile(URL, String)
     */
    @Override
    public File getFile() throws IOException {
        URL url = getURL();
        if (url.getProtocol().startsWith(ResourceAide.URL_PROTOCOL_VFS)) {
            return VfsResourceDelegate.getResource(url).getFile();
        }
        return ResourceAide.getFile(url, getDescription());
    }

    /**
     * 确定指定的 URL 指向的是否文件系统中的文件。
     *
     * @see #getFile(URI)
     */
    protected boolean isFile(URI uri) {
        try {
            if (uri.getScheme().startsWith(ResourceAide.URL_PROTOCOL_VFS)) {
                return VfsResourceDelegate.getResource(uri).isFile();
            }
            return ResourceAide.URL_PROTOCOL_FILE.equals(uri.getScheme());
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * 如果指定的 URL 指向的是文件系统中的文件，则返回其 File 引用。
     *
     * @see ResourceAide#getFile(URL, String) 
     */
    protected File getFile(URI uri) throws IOException {
        if (uri.getScheme().startsWith(ResourceAide.URL_PROTOCOL_VFS)) {
            return VfsResourceDelegate.getResource(uri).getFile();
        }
        return ResourceAide.getFile(uri, getDescription());
    }

    /**
     * 返回此资源的 FileChannel，前提是文件系统中的文件。
     *
     * @see #getFile()
     */
    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        try {
            return FileChannel.open(getFile().toPath(), StandardOpenOption.READ);
        } catch (FileNotFoundException | NoSuchFileException ex) {
            return super.readableChannel();
        }
    }

    @Override
    public long contentLength() throws IOException {
        URL url = getURL();
        if (ResourceAide.isFileURL(url)) {
            File file = getFile();
            long length = file.length();
            if (length == 0L && !file.exists()) {
                throw new FileNotFoundException(getDescription() + " Cannot be resolved in the file system for checking its content length");
            }
            return length;
        } else {
            URLConnection conn = url.openConnection();
            customizeConnection(conn);
            return conn.getContentLengthLong();
        }
    }

    @Override
    public long lastModified() throws IOException {
        URL url = getURL();
        boolean fileCheck = false;
        if (ResourceAide.isFileURL(url) || ResourceAide.isJarURL(url)) {
            fileCheck = true;
            try {
                File fileToCheck = getFileForLastModifiedCheck();
                long lastModified = fileToCheck.lastModified();
                if (lastModified > 0L || fileToCheck.exists()) {
                    return lastModified;
                }
            } catch (FileNotFoundException ex) {
                // 使用下面的后备方式：URL 连接
            }
        }
        URLConnection conn = url.openConnection();
        customizeConnection(conn);
        long lastModified = conn.getLastModified();
        if (fileCheck && lastModified == 0 && conn.getContentLengthLong() <= 0) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved in the file system for checking its last-modified timestamp");
        }
        return lastModified;
    }

    /**
     * 此实现确定要读取修改时间的底层文件，如果当前资源是 jar/zip 中的资源，则返回的是所属 jar 文件本身。
     */
    @Override
    protected File getFileForLastModifiedCheck() throws IOException {
        URL url = getURL();
        if (ResourceAide.isJarURL(url)) {
            URL actualUrl = ResourceAide.extractArchiveURL(url);
            if (actualUrl.getProtocol().startsWith(ResourceAide.URL_PROTOCOL_VFS)) {
                return VfsResourceDelegate.getResource(actualUrl).getFile();
            }
            return ResourceAide.getFile(actualUrl, "Jar URL");
        } else {
            return getFile();
        }
    }

    /**
     * <p>
     *     定制给定的 {@link URLConnection}，
     *     该 URLConnection 是在 {@link #exists()}、{@link #contentLength()} 或 {@link #lastModified()} 调用过程中获得的。
     * </p>
     * <p>
     *     调用 {@link ResourceAide#useCachesIfNecessary(URLConnection)}，
     *     并在可能的情况下委派给 {@link #customizeConnection(HttpURLConnection)}。
     *     可以在子类中覆盖。
     * </p>
     *
     * @param conn 要定制的 URLConnection
     * @throws IOException 如果从 URLConnection 的方法中抛出异常
     */
    protected void customizeConnection(URLConnection conn) throws IOException {
        ResourceAide.useCachesIfNecessary(conn);
        if (conn instanceof HttpURLConnection) {
            customizeConnection((HttpURLConnection) conn);
        }
    }

    /**
     * <p>
     *     定制给定的 {@link HttpURLConnection}，
     *     该 HttpURLConnection 是在 {@link #exists()}、{@link #contentLength()} 或 {@link #lastModified()} 调用过程中获得的。
     * </p>
     * <p>
     *     默认情况下设置请求方法为 "HEAD"，可以在子类中覆盖。
     * </p>
     *
     * @param conn 要定制的 HttpURLConnection
     * @throws IOException 如果从 HttpURLConnection 的方法中抛出异常
     */
    protected void customizeConnection(HttpURLConnection conn) throws IOException {
        conn.setRequestMethod("HEAD");
    }

    private static class VfsResourceDelegate {

        public static Resource getResource(URL url) throws IOException {
            return new VfsResource(VfsHelper.getRoot(url));
        }

        public static Resource getResource(URI uri) throws IOException {
            return new VfsResource(VfsHelper.getRoot(uri));
        }

    }
}