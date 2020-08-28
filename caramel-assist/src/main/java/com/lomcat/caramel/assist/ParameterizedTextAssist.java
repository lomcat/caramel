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

/**
 * <h3>参数化文本格式化工具的内部辅助处理类</h3>
 *
 * @author Kweny
 * @since 0.0.1
 */
class ParameterizedTextAssist {

    private static final char DELIM_START = '{';
    private static final char DELIM_STOP = '}';
    private static final char ESCAPE_CHAR = '\\';

    static String[] objectsToStrings(final Object[] args) {
        if (args == null) {

        }
        return null;
    }

    /** 计算占位符数量，并获取每个占位符的位置 */
    static int countArgumentPlaceholders(final String pattern, final int[] indices) {
        if (pattern == null) {
            return 0;
        }
        final int length = pattern.length();
        int result = 0;
        boolean isEscaped = false;
        for (int i = 0; i < length - 1; i++) {
            final char currentChar = pattern.charAt(i);
            if (currentChar == ESCAPE_CHAR) {
                isEscaped = !isEscaped;
                indices[0] = -1;
                result++;
            } else if (currentChar == DELIM_START) {
                if (!isEscaped && pattern.charAt(i + 1) == DELIM_STOP) {
                    indices[result] = i;
                    result++;
                    i++;
                }
                isEscaped = false;
            } else {
                isEscaped = false;
            }
        }
        return result;
    }
}