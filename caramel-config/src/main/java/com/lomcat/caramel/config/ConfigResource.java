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

import com.lomcat.caramel.config.internel.PriorityComparable;
import com.lomcat.caramel.core.io.Resource;
import com.lomcat.caramel.core.io.ResourceHash;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author Kweny
 * @since 0.0.1
 */
public class ConfigResource implements PriorityComparable {

    public static ConfigResource create(Resource resource, Double priority) throws NoSuchAlgorithmException, IOException {
        return new ConfigResource(resource, priority);
    }

    private final Resource resource;
    private final String hashValue;
    private final Double priority;

    public ConfigResource(Resource resource, Double priority) throws NoSuchAlgorithmException, IOException {
        this.resource = resource;
        this.hashValue = resource.getHashValue(ResourceHash.ALGORITHM_SHA256);
        this.priority = priority;
    }

    public Resource getResource() {
        return this.resource;
    }

    public String getHashValue() {
        return this.hashValue;
    }

    public Double getPriority() {
        return this.priority;
    }

    public InputStream getInputStream() throws IOException {
        return this.resource.getInputStream();
    }

    public String getDescription() {
        return this.resource.getDescription();
    }

    @Override
    public String toString() {
        return this.resource.toString();
    }
}
