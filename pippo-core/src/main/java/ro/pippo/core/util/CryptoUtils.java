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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author James Moger
 */
public class CryptoUtils {

    public static final String HMAC_MD5 = "HmacMD5";

    public static final String HMAC_SHA1 = "HmacSHA1";

    public static final String HMAC_SHA256 = "HmacSHA256";

    private CryptoUtils() {}

    public static String getHmacSHA256(String message, String secretKey) {
        return hmacDigest(message, secretKey, HMAC_SHA256);
    }

    public static String getHmacSHA1(String message, String secretKey) {
        return hmacDigest(message, secretKey, HMAC_SHA1);
    }

    public static String getHmacMD5(String message, String secretKey) {
        return hmacDigest(message, secretKey, HMAC_MD5);
    }

    public static String hmacDigest(String message, String secretKey, String algorithm) {
        String digest = null;
        try {
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(key);

            byte[] bytes = mac.doFinal(message.getBytes(StandardCharsets.US_ASCII));

            digest = toHex(bytes);
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        return digest;
    }

    /**
     * Calculates the SHA256 hash of the string.
     *
     * @param text
     * @return sha256 hash of the string
     */
    public static String getHashSHA256(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
        return getHashSHA256(bytes);
    }

    /**
     * Calculates the SHA256 hash of the byte array.
     *
     * @param bytes
     * @return sha256 hash of the byte array
     */
    public static String getHashSHA256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return toHex(digest);
        } catch (NoSuchAlgorithmException t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Calculates the SHA1 hash of the string.
     *
     * @param text
     * @return sha1 hash of the string
     */
    public static String getHashSHA1(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
        return getHashSHA1(bytes);
    }

    /**
     * Calculates the SHA1 hash of the byte array.
     *
     * @param bytes
     * @return sha1 hash of the byte array
     */
    public static String getHashSHA1(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return toHex(digest);
        } catch (NoSuchAlgorithmException t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Calculates the MD5 hash of the string.
     *
     * @param text
     * @return md5 hash of the string
     */
    public static String getHashMD5(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
        return getHashMD5(bytes);
    }

    /**
     * Calculates the MD5 hash of the byte array.
     *
     * @param bytes
     * @return md5 hash of the byte array
     */
    public static String getHashMD5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return toHex(digest);
        } catch (NoSuchAlgorithmException t) {
            throw new RuntimeException(t);
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hash.append('0');
            }
            hash.append(hex);
        }
        return hash.toString();
    }

    /**
     * Generates a random secret key.
     *
     * @return a random secret key.
     */
    public static String generateSecretKey() {
        return hmacDigest(UUID.randomUUID().toString(), UUID.randomUUID().toString(), HMAC_SHA256);
    }

}
