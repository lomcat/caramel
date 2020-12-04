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

/**
 * @author Kweny
 * @since 0.0.1
 */
public class StringAide {

    public static boolean isEmpty(final CharSequence cs) {
        return !isNotEmpty(cs);
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return cs != null && cs.length() > 0;
    }

    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static String replace(String input, String oldPattern, String newPattern) {
        if (isEmpty(input) || isEmpty(oldPattern) || newPattern == null) {
            return input;
        }

        int index = input.indexOf(oldPattern);
        if (index == -1) {
            return input;
        }

        int capacity = input.length();
        if (newPattern.length() > oldPattern.length()) {
            capacity += 16;
        }
        StringBuilder builder = new StringBuilder(capacity);

        int pos = 0;
        int patLen = oldPattern.length();
        while (index >= 0) {
            builder.append(input, pos, index);
            builder.append(newPattern);
            pos = index + patLen;
            index = input.indexOf(oldPattern, pos);
        }
        builder.append(input, pos, input.length());

        return builder.toString();
    }

}