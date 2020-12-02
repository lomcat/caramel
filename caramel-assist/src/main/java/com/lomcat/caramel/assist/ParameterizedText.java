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

package com.lomcat.caramel.assist;

import java.util.Arrays;

/**
 * <h3>参数化文本格式化工具</h3>
 *
 * @author Kweny
 * @since 0.0.1
 */
@Deprecated
public class ParameterizedText {

    /** 占位符样式 */
    public static enum PlaceholderStyle {
        /** 空花括号：{} */
        EMPTY_CURLY,
        /** 带序号的花括号：{0} */
        ORDERED_CURLY
    }

    public static String format(String pattern, Object... args) {
        return "";
    }

    private static final String INDENT = "    "; // 缩进：4个空格

    private String pattern;
    private int[] indices;
    private final String[] arguments;
    private final transient Object[] objectArguments;
    private transient int indentLevel;
    private transient String formattedText;

    public ParameterizedText(final String pattern, final Object... args) {
//        this.pattern = pattern;
        init(pattern);
        this.objectArguments = args;
        this.arguments = null; // TODO-Kweny
    }

    private void init(final String pattern) {
        this.pattern = pattern;
        final int length = Math.max(1, CaramelAide.length(pattern) >> 1); // 除以 2
        this.indices = new int[length]; // 确保数组长度非0
        final int placeholders = ParameterizedTextAssist.countArgumentPlaceholders(pattern, indices);
        System.out.println(Arrays.toString(indices));
        System.out.println(placeholders);
    }

    public static void main(String[] args) {
        new ParameterizedText("{}} aa {} bb {}", 1, 2);
        new ParameterizedText("{}} aa \\{} bb {}", 1, 2);
    }
}