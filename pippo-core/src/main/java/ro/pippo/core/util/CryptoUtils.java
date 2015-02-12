/*
 * Copyright (C) 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.core.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * @author Decebal Suiu
 */
public class CryptoUtils {

    /**
     * Computes HMAC signature.
     * HMAC allows you to ensure only the person/system holding a specific key generated this hash and no one else.
     *
     * @param key
     * @param data
     * @return The Base64-encoded HMAC signature.
     * @throws InvalidKeyException
     */
    public static String getHmacSha1(String key, String data) throws InvalidKeyException {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);

            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // TODO use java.util.Base64 from java 8
            String encoded = DatatypeConverter.printBase64Binary(hmac);

            return encoded;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
