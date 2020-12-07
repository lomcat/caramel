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
import com.lomcat.caramel.core.assist.StringAide;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Copied from
 * <a href="https://github.com/spring-projects/spring-framework/blob/v5.3.1/spring-core/src/main/java/org/springframework/util/ResourceUtils.java">
 *     org.springframework.util.ResourceUtils
 * </a>
 *
 * @author Juergen Hoeller
 * @since 0.0.1
 */
public class ResourceUtils {

    /** 类路径的伪 URL 前缀："classpath:" */
    public static final String URL_PREFIX_CLASSPATH = "classpath:";

    /** 文件系统的 URL 前缀："file:" */
    public static final String URL_PREFIX_FILE = "file:";

    /** jar 文件的 URL 前缀："jar:" */
    public static final String URL_PREFIX_JAR = "jar:";

    /** war 文件的 URL 前缀："war:" */
    public static final String URL_PREFIX_WAR = "war:";

    /** 文件系统中的文件的 URL 协议："file" */
    public static final String URL_PROTOCOL_FILE = "file";

    /** jar 文件条目的 URL 协议："jar" */
    public static final String URL_PROTOCOL_JAR = "jar";

    /** war 文件条目的 URL 协议："war" */
    public static final String URL_PROTOCOL_WAR = "war";

    /** zip 文件条目的 URL 协议："zip" */
    public static final String URL_PROTOCOL_ZIP = "zip";

    /** WebSphere jar 文件条目的 URL 协议："wsjar" */
    public static final String URL_PROTOCOL_WSJAR = "wsjar";

    /** JBoss jar 文件条目的 URL 协议："vfszip" */
    public static final String URL_PROTOCOL_VFSZIP = "vfszip";

    /** JBoss 文件系统资源的 URL 协议："vfsfile" */
    public static final String URL_PROTOCOL_VFSFILE = "vfsfile";

    /** 通用 JBoss VFS 资源的 URL 协议："vfs" */
    public static final String URL_PROTOCOL_VFS = "vfs";

    /** 常规 jar 文件的扩展名：".jar" */
    public static final String JAR_FILE_EXTENSION = ".jar";

    /** JAR URL 和 JAR 中文件路径之间的分隔符："!/" */
    public static final String JAR_URL_SEPARATOR = "!/";

    /** Tomcat 上 WAR URL 和 jar 部分之间的特殊分隔符 */
    public static final String WAR_URL_SEPARATOR = "*/";

    /** 路径分隔符："/" */
    private static final String PATH_SEPARATOR = "/";
    /** Windows 路径分隔符："\" */
    private static final String WINDOWS_PATH_SEPARATOR = "\\";
    /** 路径表示当前目录的 "." */
    private static final String CURRENT_PATH = ".";
    /** 路径表示上层目录的 ".." */
    private static final String PARENT_PATH = "..";
    /** 路径中分隔扩展名的 '.' */
    private static final char EXTENSION_SEPARATOR = '.';

    /**
     * 检查指定资源位置是否为有效的 {@link URL}：包括标准 URL 和 "classpath" 伪 URL。
     *
     * @param resourceLocation 要检查的位置字符串
     * @return 该位置字符串是否符合 URL 要求
     * @see #URL_PREFIX_CLASSPATH
     * @see java.net.URL
     */
    public static boolean isURL(String resourceLocation) {
        if (resourceLocation == null) {
            return false;
        }
        if (resourceLocation.startsWith(URL_PREFIX_CLASSPATH)) {
            return true;
        }
        try {
            new URL(resourceLocation);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    /**
     * 将指定的资源位置解析为 {@code URL}。不检查 URL 是否实际存在，只返回指定位置对应的 URL 对象。
     *
     * @param resourceLocation 要解析的资源位置，如 "classpath:" URL、"file:" URL 以及纯文件路径
     * @return 对应的 URL 对象
     * @throws FileNotFoundException 如果资源位置无法解析为 URL
     */
    public static URL getURL(String resourceLocation) throws FileNotFoundException {
        AssertAide.notNull(resourceLocation, "Resource location must not be null");
        if (resourceLocation.startsWith(URL_PREFIX_CLASSPATH)) {
            String path = resourceLocation.substring(URL_PREFIX_CLASSPATH.length());
            ClassLoader cl = ClassUtils.getDefaultClassLoader();
            URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                String description = "class path resource [" + path + "]";
                throw new FileNotFoundException(description + " cannot be resolved to URL because it does not exist");
            }
            return url;
        }
        try {
            return new URL(resourceLocation);
        } catch (MalformedURLException ex) {
            try {
                return new File(resourceLocation).toURI().toURL();
            } catch (MalformedURLException ex2) {
                throw new FileNotFoundException("Resource location [" + resourceLocation + "] is neither a URL not a well-formed file path");
            }
        }
    }

    /**
     * 将指定的资源位置解析为 {@link File}，即文件系统中的文件。不检查文件是否实际存在，只返回位置对应的 File 对象。
     *
     * @param resourceLocation 要解析的资源位置，如 "classpath:" URL、"file:" URL 以及纯文件路径
     * @return 对应的 File 对象
     * @throws FileNotFoundException 如果资源位置无法解析为文件系统中的文件
     */
    public static File getFile(String resourceLocation) throws FileNotFoundException {
        AssertAide.notNull(resourceLocation, "Resource location must not be null");
        if (resourceLocation.startsWith(URL_PREFIX_CLASSPATH)) {
            String path = resourceLocation.substring(URL_PREFIX_CLASSPATH.length());
            String description = "class path resource [" + path + "]";
            ClassLoader cl = ClassUtils.getDefaultClassLoader();
            URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                throw new FileNotFoundException(description + " cannot be resolved to absolute file path because it does not exist");
            }
            return getFile(url, description);
        }
        try {
            // 尝试使用 URL 获取
            return getFile(new URL(resourceLocation));
        } catch (MalformedURLException ex) {
            // 无效的 URL，视为文件路径
            return new File(resourceLocation);
        }
    }

    /**
     * 将指定的资源 URL 解析为 {@link File}，即文件系统中的文件。不检查文件是否实际存在，只返回位置对应的 File 对象。
     *
     * @param resourceUrl 要解析的资源 URL
     * @return 对应的 File 对象
     * @throws FileNotFoundException 如果 URL 无法解析为文件系统中的文件
     */
    public static File getFile(URL resourceUrl) throws FileNotFoundException {
        return getFile(resourceUrl, "URL");
    }

    /**
     * 将指定的资源 URL 解析为 {@link File}，即文件系统中的文件。不检查文件是否实际存在，只返回位置对应的 File 对象。
     *
     * @param resourceUrl 要解析的资源 URL
     * @param description 创建 URL 时的原始资源描述，如 classpath 位置
     * @return 对应的 File 对象
     * @throws FileNotFoundException 如果 URL 无法解析为文件系统中的文件
     */
    public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
        AssertAide.notNull(resourceUrl, "Resource URL must not be null");
        if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
            throw new FileNotFoundException(description + " cannot be resolved to absolute file path because it does not reside in the file system: " + resourceUrl);
        }
        try {
            return new File(toURI(resourceUrl).getSchemeSpecificPart());
        } catch (URISyntaxException ex) {
            // 备用：不是有效的 URL（几乎不会发生）
            return new File(resourceUrl.getFile());
        }
    }

    /**
     * 将指定的资源 URI 解析为 {@link File}，即文件系统中的文件。不检查文件是否实际存在，只返回位置对应的 File 对象。
     *
     * @param resourceUri 要解析的资源 URI
     * @return 对应的 File 对象
     * @throws FileNotFoundException 如果 URI 无法解析为文件系统中的文件
     */
    public static File getFile(URI resourceUri) throws FileNotFoundException {
        return getFile(resourceUri, "URI");
    }

    /**
     * 将指定的资源 URI 解析为 {@link File}，即文件系统中的文件。不检查文件是否实际存在，只返回位置对应的 File 对象。
     *
     * @param resourceUri 要解析的资源 URI
     * @param description 创建 URI 时的原始资源描述，如 classpath 位置
     * @return 对应的 File 对象
     * @throws FileNotFoundException 如果 URI 无法解析为文件系统中的文件
     */
    public static File getFile(URI resourceUri, String description) throws FileNotFoundException {
        AssertAide.notNull(resourceUri, "Resource URI must not be null");
        if (!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
            throw new FileNotFoundException(description + " cannot be resolved to absolute file path because it does not reside in the file system: " + resourceUri);
        }
        return new File(resourceUri.getSchemeSpecificPart());
    }

    /**
     * 检查给定的 URL 是否指向文件系统中的资源，即是否具有协议 "file"、"vfsfile"、"vfs"。
     *
     * @param url 要检查的 URL
     * @return 该 URL 是否被识别为文件系统 URL
     */
    public static boolean isFileURL(URL url) {
        String protocol = url.getProtocol();
        return URL_PROTOCOL_FILE.equals(protocol)
                || URL_PROTOCOL_VFSFILE.equals(protocol)
                || URL_PROTOCOL_VFS.equals(protocol);
    }

    /**
     * 检查指定的 URL 是否指向 jar 文件中的资源，即具有 "jar"、"war"、"zip"、"vfszip" 、"wsjar" 协议。
     *
     * @param url 要检查的 URL
     * @return 该 URL 是否被识别为 JAR URL
     */
    public static boolean isJarURL(URL url) {
        String protocol = url.getProtocol();
        return URL_PROTOCOL_JAR.equals(protocol)
                || URL_PROTOCOL_WAR.equals(protocol)
                || URL_PROTOCOL_ZIP.equals(protocol)
                || URL_PROTOCOL_VFSZIP.equals(protocol)
                || URL_PROTOCOL_WSJAR.equals(protocol);
    }

    /**
     * 检查指定的 URL 是否指向 jar 文件本身，即具有 "file" 协议，且扩展名为 ".jar"。
     *
     * @param url 要检查的 URL
     * @return 该 URL 是否被识别为 JAR 文件 URL
     */
    public static boolean isJarFileURL(URL url) {
        return URL_PROTOCOL_FILE.equals(url.getProtocol())
                && url.getPath().toLowerCase().endsWith(JAR_FILE_EXTENSION);
    }

    /**
     * 从指定的 URL（可能是 jar 文件中的资源，也可能是 jar 文件本身）中提取实际 jar 文件的 URL。
     *
     * @param jarUrl 原始 URL
     * @return 实际 jar 文件的 URL
     * @throws MalformedURLException 如果无法提取有效 jar 文件 URL
     */
    public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
        String urlFile = jarUrl.getFile();
        int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
        if (separatorIndex != -1) {
            String jarFile = urlFile.substring(0, separatorIndex);
            try {
                return new URL(jarFile);
            } catch (MalformedURLException ex) {
                // 原始 jar URL 中可能没有协议，如 "jar:C:/mypath/myjar.jar"，这通常表明 jar 文件位于文件系统中
                if (!jarFile.startsWith("/")) {
                    jarFile = "/" + jarFile;
                }
                return new URL(URL_PROTOCOL_FILE + jarFile);
            }
        } else {
            return jarUrl;
        }
    }

    /**
     * 从指定的 jar/war URL（可能是 jar 文件中的资源，也可能是 jar 文件本身）中提取最外层存档的 URL。
     *
     * <p>如果 jar 文件嵌套在 war 文件中，则返回 war 文件的 URL，因为这是文件系统可解析的 URL。</p>
     *
     * @param jarUrl 原始 URL
     * @return 实际 jar 文件的 URL
     * @throws MalformedURLException 如果无法提取有效 jar 文件 URL
     * @see #extractJarFileURL(URL)
     */
    public static URL extractArchiveURL(URL jarUrl) throws MalformedURLException {
        String urlFile = jarUrl.getFile();
        int endIndex = urlFile.indexOf(WAR_URL_SEPARATOR);
        if (endIndex != -1) {
            // Tomcat 的 war URL "war:file:...mywar.war*/WEB-INF/lib/myjar.jar!/myentry.txt"
            String warFile = urlFile.substring(0, endIndex);
            if (URL_PROTOCOL_WAR.equals(jarUrl.getProtocol())) {
                return new URL(warFile);
            }
            int startIndex = warFile.indexOf(URL_PREFIX_WAR);
            if (startIndex != -1) {
                return new URL(warFile.substring(startIndex + URL_PREFIX_WAR.length()));
            }
        }
        // jar URL "jar:file:...myjar.jar!/myentry.txt"
        return extractJarFileURL(jarUrl);
    }

    /**
     * 为指定的 URL 创建一个 URI 实例，将首先用 URI 编码 "%20" 替换空格。
     *
     * @param url 要转换为 URI 的 URL
     * @return URI 实例
     * @throws URISyntaxException 如果 URL 不是有效的 URI
     * @see java.net.URL#toURI()
     */
    public static URI toURI(URL url) throws URISyntaxException {
        return toURI(url.toString());
    }

    /**
     * 为指定的位置字符串创建一个 URI 实例，将首先用 URI 编码 "%20" 替换空格。
     *
     * @param location 要转换为 URI 的位置字符串
     * @return URI 实例
     * @throws URISyntaxException 如果位置字符串不是有效的 URI
     */
    public static URI toURI(String location) throws URISyntaxException {
        return new URI(StringAide.replace(location, " ", "%20"));
    }

    /**
     * 在指定的连接上设置 {@link URLConnection#setUseCaches "useCaches"} 标志，
     * 首选为  {@code false}，但对于基于 JNLP 的资源，将该标志保留为 {@code true}。
     *
     * @param con 要设置标志的 URLConnection
     */
    public static void useCachesIfNecessary(URLConnection con) {
        con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
    }

    /**
     * <p>通过抑制类似 {@code "path/.."} 中表示当前目录和上层目录的简单点 {@code "."}、{@code ".."} 之类的元素来规范化路径。</p>
     * <p>返回结果方便用于路径比较。</p>
     * <p>注意，Windows 分隔符 {@code "\"}（反斜杠） 将被替换为斜杠 {@code "/"}。</p>
     *
     * @param path 原始路径
     * @return 规范化的路径
     */
    public static String normalizePath(String path) {
        if (StringAide.isEmpty(path)) {
            return path;
        }

        // Windows 分隔符 "\"（反斜杠） 将被替换为斜杠 "/"
        String pathToUse = StringAide.replace(path, WINDOWS_PATH_SEPARATOR, PATH_SEPARATOR);

        // 如果路径中不包含 "."、".." 等简单点路径元素，则无需处理后续处理
        if (pathToUse.indexOf('.') == -1) {
            return pathToUse;
        }

        // 如果路径中包含前缀，如 "file:"、"classpath:" 等，则将前缀从路径中删除以便后续处理，
        // 这对于正确解析如 "file:core/../core/io/Resource.class" 之类的路径是必要的，
        // 路径中的 ".." 将抵消掉其前面的 "core"，同时保留 "file:" 前缀。
        // 如果前缀中包含斜杠 "/"，则认为其不是前缀，而是路径元素，则前缀为空，
        // 例如 "fil/e:core/io/../Resource.class" 认为是 fil 下的 e:core 下的 Resource.class，"io" 被其后的 ".." 抵消；
        // 否则若前缀中不含斜杠，则将其从路径中删除以便后续处理
        int prefixIndex = pathToUse.indexOf(':');
        String prefix = "";
        if (prefixIndex != -1) {
            // 获取前缀
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains(PATH_SEPARATOR)) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }
        // 如果去除前缀后的路径以斜杠 "/" 开头，则将 "/" 转移到前缀
        if (pathToUse.startsWith(PATH_SEPARATOR)) {
            prefix = prefix + PATH_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = pathToUse.split(PATH_SEPARATOR);
        Deque<String> pathElements = new ArrayDeque<>(); // 存储处理后的路径元素
        int parents = 0; // 记录 ".." 的个数
        // 从后向前检查路径元素，
        // "." 元素表示当前目录，可以忽略，
        // ".." 元素表示上级目录，需要进行计数，当后续每向前检查到一级标准的路径元素，则抵消掉一个 ".."
        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (CURRENT_PATH.equals(element)) {
                // 一个点 "."，表示当前目录 - 忽略
                continue;
            }
            if (PARENT_PATH.equals(element)) {
                // 两个点 ".."，表示上层目录 - 记录个数
                parents++;
            } else {
                // 非“简单点”的标准路径元素，如果还存在 ".."（tops > 0），则抵消，否则存储该路径元素
                if (parents > 0) {
                    parents --;
                } else {
                    pathElements.addFirst(element);
                }
            }
        }

        // 每一层的路径元素不变，直接返回
        if (pathArray.length == pathElements.size()) {
            return prefix + pathToUse;
        }
        // 如果还存在 ".." 没有被抵消干净，加到路径首
        for (int i = 0; i < parents; i++) {
            pathElements.addFirst(PARENT_PATH);
        }
        // 如果最终剩下的只有一个空串的路径元素，且前缀不是以斜杠 "/" 结束的表示根路径，则应指向当前路径
        if (pathElements.size() == 1 && pathElements.getLast().isEmpty() && !prefix.endsWith(PATH_SEPARATOR)) {
            pathElements.addFirst(CURRENT_PATH);
        }

        // 拼接前缀和规范化之后的路径并返回
        return prefix + String.join(PATH_SEPARATOR, pathElements);
    }

    /**
     * 使用指定的资源路径（假定为标准 Java 文件夹分隔，即 "/" 分隔符），应用新的相对路径。
     *
     * <p>如资源路径 "C:/path/to/file" 应用相对路径 "file2" 得当新的完整路径 "C:/path/to/file2"。
     *
     * @param path 起始资源路径（通常是完整文件路径）
     * @param relativePath 要应用的相对路径（相对于前面的完整文件路径）
     * @return 应用相对路径后产生的新的完整文件路径
     */
    public static String applyRelativePath(String path, String relativePath) {
        int separatorIndex = path.lastIndexOf(PATH_SEPARATOR);
        if (separatorIndex == -1) {
            return relativePath;
        }
        String newPath = path.substring(0, separatorIndex);
        if (!relativePath.startsWith(PATH_SEPARATOR)) {
            newPath += PATH_SEPARATOR;
        }
        return newPath + relativePath;
    }

    /**
     * 从指定的 Java 资源路径中提取文件名，如 {@code "path/file.txt" -> "file.txt"}。
     *
     * @param path 资源路径，可以为 null
     * @return 提取的文件名，如果没有则返回 null
     */
    public static String getFilename(String path) {
        if (path == null) {
            return null;
        }
        int separatorIndex = path.lastIndexOf(PATH_SEPARATOR);
        return separatorIndex != -1 ? path.substring(separatorIndex + 1) : path;
    }

    /**
     * 从指定的 Java 资源路径中提取文件扩展名，如 {@code "path/file.txt" -> "txt"}。
     *
     * @param path 文件路径，可以为 null
     * @return 提取的文件扩展名，如果没有则返回 null
     */
    public static String getFilenameExtension(String path) {
        if (path == null) {
            return null;
        }

        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return null;
        }

        int folderIndex = path.lastIndexOf(PATH_SEPARATOR);
        if (folderIndex > extIndex) {
            return null;
        }

        return path.substring(extIndex + 1);
    }

    /**
     * 从指定的 Java 资源路径中删除文件扩展名，如 {@code "path/file.txt" -> "path/file"}。
     *
     * @param path 文件路径
     * @return 删除扩展名之后的路径
     */
    public static String stripFilenameExtension(String path) {
        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return path;
        }

        int folderIndex = path.lastIndexOf(PATH_SEPARATOR);
        if (folderIndex > extIndex) {
            return path;
        }

        return path.substring(0, extIndex);
    }

}