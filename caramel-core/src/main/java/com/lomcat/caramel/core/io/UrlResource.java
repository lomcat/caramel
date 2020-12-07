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

import com.lomcat.caramel.core.assist.AssertAide;
import com.lomcat.caramel.core.assist.StringAide;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * Copied from
 * <a href="https://github.com/spring-projects/spring-framework/blob/v5.3.1/spring-core/src/main/java/org/springframework/core/io/UrlResource.java">
 *     org.springframework.core.io.UrlResource
 * </a>
 *
 * <p>
 *     {@link java.net.URL} 定位器的 {@link Resource} 实现。
 *     在 {@code "file:"} 协议下，支持解析为 {@code URL} 和 {@code File}。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 0.0.1
 * @see java.net.URL
 */
public class UrlResource extends AbstractFileResolvingResource {

    /**
     * 原始 URI（如果有），用于 URI 和文件访问
     */
    private final URI uri;

    /**
     * 原始 URL，用于实际访问
     */
    private final URL url;

    /**
     * 清理过的 URL（具有标准化的路径），用于比较
     */
    private volatile URL cleanedUrl;


    /**
     * 基于指定的 URI 对象创建一个新的 {@link UrlResource}。
     *
     * @param uri 一个 URI
     * @throws MalformedURLException 如果指定的 URL 路径无效
     */
    public UrlResource(URI uri) throws MalformedURLException {
        AssertAide.notNull(uri, "URI must not be null");
        this.uri = uri;
        this.url = uri.toURL();
    }

    /**
     * 基于指定的 URL 对象创建一个新的 {@link UrlResource}。
     *
     * @param url 一个 URL
     */
    public UrlResource(URL url) {
        AssertAide.notNull(url, "URL must not be null");
        this.uri = null;
        this.url = url;
    }

    /**
     * 基于指定的 URL 路径创建一个新的 {@link UrlResource}。
     *
     * <p>如有必要，指定的路径需要进行预编码。
     *
     * @param path 一个 URL 路径
     * @throws MalformedURLException 如果指定的 URL 路径无效
     * @see java.net.URL#URL(String)
     */
    public UrlResource(String path) throws MalformedURLException {
        AssertAide.notNull(path, "Path must not be null");
        this.uri = null;
        this.url = new URL(path);
        this.cleanedUrl = getCleanedUrl(this.url, path);
    }

    /**
     * 根据 URI 规范创建一个新的 {@link UrlResource}。
     *
     * <p>必要时会自动进行编码。
     *
     * @param protocol 要使用的 URL 协议（如 "jar"、"file"，不带冒号），也被称为 "scheme"
     * @param location 位置（如指定协议下的文件路径），也被成为 "scheme-specific part"
     * @throws MalformedURLException 如果指定的 URL 规范无效
     * @see java.net.URI#URI(String, String, String)
     */
    public UrlResource(String protocol, String location) throws MalformedURLException  {
        this(protocol, location, null);
    }

    /**
     * 根据 URI 规范创建一个新的 {@link UrlResource}。
     *
     * <p>必要时会自动进行编码。
     *
     * @param protocol 要使用的 URL 协议（如 "jar"、"file"，不带冒号），也被称为 "scheme"
     * @param location 位置（如指定协议下的文件路径），也被成为 "scheme-specific part"
     * @param fragment 位置中的片段（如 HTML 页面中的锚点，在 "#" 分隔符之后）
     * @throws MalformedURLException 如果指定的 URL 规范无效
     * @see java.net.URI#URI(String, String, String)
     */
    public UrlResource(String protocol, String location, String fragment) throws MalformedURLException  {
        try {
            this.uri = new URI(protocol, location, fragment);
            this.url = this.uri.toURL();
        } catch (URISyntaxException ex) {
            MalformedURLException exToThrow = new MalformedURLException(ex.getMessage());
            exToThrow.initCause(ex);
            throw exToThrow;
        }
    }


    /**
     * 返回对原始 URL 规范化处理后的 URL。
     *
     * @param originalUrl 原始 URL
     * @param originalPath 原始 URL 路径
     * @return 规范化的 URL（可能是原始 URL 的原样）
     * @see ResourceUtils#normalizePath(String)
     */
    private static URL getCleanedUrl(URL originalUrl, String originalPath) {
        String cleanedPath = ResourceUtils.normalizePath(originalPath);
        if (!cleanedPath.equals(originalPath)) {
            try {
                return new URL(cleanedPath);
            } catch (MalformedURLException ex) {
                // 规范化处理后的路径无法创建为 URL -> 采用原始 URL
            }
        }
        return originalUrl;
    }

    /**
     * 懒惰地为原始 URL 确定一个规范化的 URL。
     *
     * @see #getCleanedUrl(URL, String)
     */
    private URL getCleanedUrl() {
        URL cleanedUrl = this.cleanedUrl;
        if (cleanedUrl != null) {
            return cleanedUrl;
        }
        cleanedUrl = getCleanedUrl(this.url, (this.uri != null ? this.uri : this.url).toString());
        this.cleanedUrl = cleanedUrl;
        return cleanedUrl;
    }


    /**
     * 此实现打开当前 URL 资源的 InputStream。
     *
     * <p>将 {@code useCaches} 标记设置成了 {@code false}，主要是为了避免 Windows 上的 jar 文件锁定。
     *
     * @see java.net.URL#openConnection()
     * @see java.net.URLConnection#setUseCaches(boolean)
     * @see java.net.URLConnection#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        URLConnection con = this.url.openConnection();
        ResourceUtils.useCachesIfNecessary(con);
        try {
            return con.getInputStream();
        } catch (IOException ex) {
            // 关闭 HTTP 连接（如果适用）
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }

    /**
     * 此实现返回底层 URL 引用。
     */
    @Override
    public URL getURL() {
        return this.url;
    }

    /**
     * 如果可能，此实现将直接返回底层 URI。
     */
    @Override
    public URI getURI() throws IOException {
        if (this.uri != null) {
            return this.uri;
        } else {
            return super.getURI();
        }
    }

    @Override
    public boolean isFile() {
        if (this.uri != null) {
            return super.isFile(this.uri);
        }
        else {
            return super.isFile();
        }
    }

    /**
     * 如果当前资源的底层 URL/URI 指向的是文件系统的文件，则返回对应 File 引用。
     *
     * @see ResourceUtils#getFile(java.net.URL, String)
     */
    @Override
    public File getFile() throws IOException {
        if (this.uri != null) {
            return super.getFile(this.uri);
        } else {
            return super.getFile();
        }
    }

    /**
     * 此实现创建一个 {@code UrlResource}，委派给 {@link #createRelativeURL(String)} 以适配相对路径。
     *
     * @see #createRelativeURL(String)
     */
    @Override
    public Resource createRelative(String relativePath) throws MalformedURLException {
        return new UrlResource(createRelativeURL(relativePath));
    }

    /**
     * 使用指定的相对路径创建一个基于当前资源的相对（路径）URL。
     * 指定的相对路径前导斜杠将被删除，"#" 将被编码为 "%23"。
     *
     * @see #createRelative(String)
     * @see java.net.URL#URL(java.net.URL, String)
     */
    protected URL createRelativeURL(String relativePath) throws MalformedURLException {
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        // # 符号可以出现在文件名中，java.net.URL 不应将其视为 URL 片段（fragment），因此将 # 编码为 %23
        relativePath = StringAide.replace(relativePath, "#", "%23");
        // 使用 URL 的构造函数来应用相对路径以符合 URL 规范
        return new URL(this.url, relativePath);
    }

    /**
     * 此实现返回当前 URL 指向的文件的名称。
     *
     * @see java.net.URL#getPath()
     */
    @Override
    public String getFilename() {
        return ResourceUtils.getFilename(getCleanedUrl().getPath());
    }

    /**
     * 此实现返回包含当前 URL 的描述。
     */
    @Override
    public String getDescription() {
        return "URL [" + this.url + "]";
    }


    /**
     * 此实现比较底层 URL 引用。
     */
    @Override
    public boolean equals(Object other) {
        return this == other ||
                (other instanceof UrlResource && getCleanedUrl().equals(((UrlResource) other).getCleanedUrl()));
    }

    /**
     * 此实现返回底层 URL 引用的哈希码。
     */
    @Override
    public int hashCode() {
        return getCleanedUrl().hashCode();
    }

}