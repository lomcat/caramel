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

package com.lomcat.caramel.core.assist;

import java.io.Closeable;
import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Kweny
 * @since 0.0.1
 */
public abstract class ClassAide {

    /** 数组类名称的后缀：{@code "[]"} */
    public static final String ARRAY_SUFFIX = "[]";

    /** 内部数组类名称的前缀：{@code "["} */
    private static final String INTERNAL_ARRAY_PREFIX = "[";

    /** 内部非原始数组类名称前缀：{@code "[L"} */
    private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

//    /** 一个可重用的空的 Class 数组常量 */
//    private static final Class<?>[] EMPTY_CLASS_ARRAY = {};

    /** Java 包路径分隔符 '.' */
    private static final char PACKAGE_SEPARATOR = '.';

    /** 路径分隔符："/" */
    private static final char PATH_SEPARATOR = '/';

    /** 内部类分隔符：{@code '$'} */
    private static final char INNER_CLASS_SEPARATOR = '$';

    /** CGLIB 类分隔符：{@code "$$"} */
    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    /** ".class" 文件后缀 */
    public static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * 以原始包装类型作为键，以对应原始类型作为值的映射，如 Integer.class -> int.class。
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);

//    /**
//     * 以原始类型为键，以对应的包装类型为值的映射，如 int.class -> Integer.class。
//     */
//    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(8);

    /**
     * 以原始类名称为键，以对应原始类型为值的映射，如 "int" -> int.class
     */
    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<>(32);

    /**
     * 以通用 Java 语言类名称作为键，以对应的 Class 为值的映射，主要是为了对远程调用进行有效的反序列化。
     */
    private static final Map<String, Class<?>> commonClassCache = new HashMap<>(64);

//    /**
//     * 搜索“主要”用户级接口时应忽略的通用 Java 语言接口。
//     */
//    private static final Set<Class<?>> javaLanguageInterfaces;

//    /**
//     * Cache for equivalent methods on an interface implemented by the declaring class.
//     */
//    private static final Map<Method, Method> interfaceMethodCache = new ConcurrentReferenceHashMap<>(256);

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);
        primitiveWrapperTypeMap.put(Void.class, void.class);

        for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
//            primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
            registerCommonClasses(entry.getKey());
        }

        Set<Class<?>> primitiveTypes = new HashSet<>(32);
        primitiveTypes.addAll(primitiveWrapperTypeMap.values());
        Collections.addAll(primitiveTypes, boolean[].class, byte[].class, char[].class,
                double[].class, float[].class, int[].class, long[].class, short[].class);
        for (Class<?> primitiveType : primitiveTypes) {
            primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
        }

        registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class,
                Float[].class, Integer[].class, Long[].class, Short[].class);
        registerCommonClasses(Number.class, Number[].class, String.class, String[].class,
                Class.class, Class[].class, Object.class, Object[].class);
        registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class,
                Error.class, StackTraceElement.class, StackTraceElement[].class);
        registerCommonClasses(Enum.class, Iterable.class, Iterator.class, Enumeration.class,
                Collection.class, List.class, Set.class, Map.class, Map.Entry.class, Optional.class);

        Class<?>[] javaLanguageInterfaceArray = {Serializable.class, Externalizable.class,
                Closeable.class, AutoCloseable.class, Cloneable.class, Comparable.class};
        registerCommonClasses(javaLanguageInterfaceArray);
//        javaLanguageInterfaces = new HashSet<>(Arrays.asList(javaLanguageInterfaceArray));
    }

    /**
     * 使用 ClassAide 缓存注册指定的通用类。
     */
    private static void registerCommonClasses(Class<?>... commonClasses) {
        for (Class<?> clazz : commonClasses) {
            commonClassCache.put(clazz.getName(), clazz);
        }
    }

    /**
     * <p>
     *     获取默认的 ClassLoader。
     *     通常是线程上下文 ClassLoader；
     *     若没有，则返回该类 {@link ClassAide} 的 ClassLoader：{@code ClassAide.class.getClassLoader()}；
     *     若仍没有（null），则可能是引导类加载器，此时返回系统类加载器：{@code ClassLoader.getSystemClassLoader()}；
     *     若还没有，返回 null。
     * </p>
     *
     * @return 默认的 ClassLoader，可能返回 null
     * @see Thread#getContextClassLoader()
     * @see ClassLoader#getSystemClassLoader()
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader loader = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // 无法访问线程上下文类加载器 - 使用下面的备用方式
        }
        if (loader == null) {
            // 无线程上下文类加载器 -> 备用方式1：使用当前类的类加载器
            loader = ClassAide.class.getClassLoader();

            if (loader == null) {
                // 当备用1的 getClassLoader() 返回 null 时表示是引导类加载器 -> 备用方式2：返回系统类加载器
                try {
                    loader = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // 无法访问系统类加载器 -> 返回 null
                }
            }
        }
        return loader;
    }

    /**
     * 指定一个类对象，返回由该类的包名组成的字符串作为路径，即将包名分隔符 '.' 替换为路径分隔符 '/'。不会添加前导斜杠或尾部斜杠。
     * 可以将结果与斜杠和资源名连接起来，然后直接输入到 {@link ClassLoader#getResource(String)} 来获取资源。
     * 对于 {@link Class#getResource(String)} 需要加一个前导斜杠。
     *
     * @param clazz 指定类，{@code null} 或默认（空）包路径将返回空字符串
     * @return 使用包名表示的路径
     * @see ClassLoader#getResource
     * @see Class#getResource
     */
    public static String classPackageAsResourcePath(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        String className = clazz.getName();
        int packageEndIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        if (packageEndIndex == -1) {
            return "";
        }
        String packageName = className.substring(0, packageEndIndex);
        return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {
        AssertAide.notNull(name, "Name must not be null");

        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz == null) {
            clazz = commonClassCache.get(name);
        }
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" 风格的数组
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" 风格的数组
        if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
            String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" 或者 "[[Ljava.lang.String;" 风格的数组
        if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
            String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader clToUse = classLoader;
        if (clToUse == null) {
            clToUse = getDefaultClassLoader();
        }
        try {
            return Class.forName(name, false, clToUse);
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR);
            if (lastDotIndex != -1) {
                String innerClassName = name.substring(0, lastDotIndex) + INNER_CLASS_SEPARATOR + name.substring(lastDotIndex + 1);
                try {
                    return Class.forName(innerClassName, false, clToUse);
                } catch (ClassNotFoundException ex2) {
                    // 忽略此处异常，后续抛出原始异常
                }
            }
            throw ex;
        }
    }

    /**
     * 根据 JVM 对原始类的命名规则，将指定的类名称解析为原始类。
     * 支持原始数组的 JVM 内部类名称。
     * 不支持原始数组的后缀 "[]"，这仅在 {@link #forName(String, ClassLoader)} 中支持。
     *
     * @param name 可能的原始类名称
     * @return 原始类，如果名称不表示原始类或原始数组类，则返回 {@code null}
     */
    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // 考虑到存在包路径，多数类名都会很长，因此进行长度检查是值得的
        if (name != null && name.length() <= 7) {
            // 可能是原始类型
            result = primitiveTypeNameMap.get(name);
        }
        return result;
    }

}