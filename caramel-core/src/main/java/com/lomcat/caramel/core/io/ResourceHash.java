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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TODO-Kweny ResourceHash
 *
 * @author Kweny
 * @since 0.0.1
 */
public class ResourceHash {

    public static final String ALGORITHM_MD5 = "MD5";
    public static final String ALGORITHM_SHA1 = "SHA-1";
    public static final String ALGORITHM_SHA224 = "SHA-224";
    public static final String ALGORITHM_SHA256 = "SHA-256";
    public static final String ALGORITHM_SHA384 = "SHA-384";
    public static final String ALGORITHM_SHA512 = "SHA-512";

    public static String hashValue(Resource resource) throws NoSuchAlgorithmException, IOException {
        return hashValue(resource, ALGORITHM_MD5);
    }

    public static String hashValue(Resource resource, String algorithm) throws NoSuchAlgorithmException, IOException {
        try (InputStream is = resource.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = is.read(buffer, 0, buffer.length)) != -1) {
                digest.update(buffer, 0, length);
            }
            byte[] hashBytes = digest.digest();
            return new BigInteger(1, hashBytes).toString(16);
        }
    }
}
