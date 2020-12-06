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

package com.lomcat.caramel.config.internel;

import org.jetbrains.annotations.NotNull;

/**
 * @author Kweny
 * @since 0.0.1
 */
public interface PriorityComparable extends Comparable<PriorityComparable> {

    Double priority();

    @Override
    default int compareTo(@NotNull PriorityComparable other) {
        if (this.priority() == null && other.priority() == null) {
            return 0;
        }

        if (this.priority() == null) {
            return -1;
        }

        if (other.priority() == null) {
            return 1;
        }

        return this.priority().equals(other.priority()) ? 0 : (this.priority() > other.priority() ? 1 : -1);
    }

}