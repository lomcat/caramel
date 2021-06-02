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

package com.lomcat.caramel.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TODO-Kweny CaramelConfigCache
 *
 * @author Kweny
 * @since 0.0.1
 */
class _CaramelConfigCache {

    // 初级缓存 < key, <name, bunch> >
    private static final Map<String, Map<String, _CachedConfigBunch>> INITIAL_CACHE = new ConcurrentHashMap<>();
    private static final ReentrantReadWriteLock INITIAL_CACHE_LOCK = new ReentrantReadWriteLock();
    private static final Set<String> CHANGED_KEYS = new HashSet<>();

    // 终级缓存
    private static final Map<String, CaramelConfig> FINAL_CACHE = new ConcurrentHashMap<>();
    private static final ReentrantReadWriteLock FINAL_CACHE_LOCK = new ReentrantReadWriteLock();

    static void cacheInitial(_CachedConfigBunch bunch) {
        INITIAL_CACHE_LOCK.writeLock().lock();
        try {
            Map<String, _CachedConfigBunch> bunchMap = INITIAL_CACHE.computeIfAbsent(bunch.getKey(), key -> new ConcurrentHashMap<>());
            bunchMap.put(bunch.getName(), bunch);
            CHANGED_KEYS.add(bunch.getKey());
        } finally {
            INITIAL_CACHE_LOCK.writeLock().unlock();
        }
    }

    static void refreshFinalCache() {
        CHANGED_KEYS.clear();
    }
}
