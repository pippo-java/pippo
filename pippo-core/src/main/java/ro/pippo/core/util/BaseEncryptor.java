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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * @author Decebal Suiu
 */
public class BaseEncryptor implements Encryptor {

    private String algorithm;

    public BaseEncryptor(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String encrypt(String key, String data) throws Exception {
        Key secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // TODO use java.util.Base64 from java 8
        String encoded = DatatypeConverter.printBase64Binary(encrypted);

        return encoded;
    }

    @Override
    public String decrypt(String key, String data) throws Exception {
        Key secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // TODO use java.util.Base64 from java 8
        byte[] decoded = DatatypeConverter.parseBase64Binary(data);

        String decrypted = new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);

        return decrypted;
    }

}
