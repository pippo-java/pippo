/*
 * Copyright (C) 2016 the original author or authors.
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
package ro.pippo.session.encryptor.transcoder;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.CryptoUtils;
import ro.pippo.session.SerializationSessionDataTranscoder;
import ro.pippo.session.SessionData;
import ro.pippo.session.SessionDataTranscoder;

/**
 * @author Herman Barrantes
 */
public class EncryptorSessionDataTranscoder implements SessionDataTranscoder {

    private static final String CHECKSUM_KEY = "_cs";
    private final String secretKey;
    private final String hmacSHA1Key;
    private final Encryptor encryptor;
    private final SessionDataTranscoder transcoder;

    public EncryptorSessionDataTranscoder(String secretKey) {
        this(secretKey, secretKey, new SerializationSessionDataTranscoder(), new DefaultEncryptor());
    }

    public EncryptorSessionDataTranscoder(String secretKey, String hmacSHA1Key) {
        this(secretKey, hmacSHA1Key, new SerializationSessionDataTranscoder(), new DefaultEncryptor());
    }

    public EncryptorSessionDataTranscoder(String secretKey, Encryptor encryptor) {
        this(secretKey, secretKey, new SerializationSessionDataTranscoder(), encryptor);
    }

    public EncryptorSessionDataTranscoder(String secretKey, SessionDataTranscoder transcoder) {
        this(secretKey, secretKey, transcoder, new DefaultEncryptor());
    }

    public EncryptorSessionDataTranscoder(String secretKey, String hmacSHA1Key, SessionDataTranscoder transcoder, Encryptor encryptor) {
        this.secretKey = secretKey;
        this.hmacSHA1Key = hmacSHA1Key;
        this.transcoder = transcoder;
        this.encryptor = encryptor;
    }

    @Override
    public String encode(SessionData sessionData) {
        try {
            String checksum = checksumSessionData(sessionData);
            sessionData.put(CHECKSUM_KEY, checksum);
            String data = transcoder.encode(sessionData);
            return encryptor.encrypt(data, secretKey);
        } catch (Exception ex) {
            throw new PippoRuntimeException(ex);
        }
    }

    @Override
    public SessionData decode(String data) {
        try {
            data = encryptor.decrypt(data, secretKey);
            SessionData sessionData = transcoder.decode(data);
            if (isValidSessionData(sessionData)) {
                return sessionData;
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new PippoRuntimeException(ex);
        }
    }

    protected String checksumSessionData(SessionData sessionData) {
        String data = transcoder.encode(sessionData);
        return CryptoUtils.getHmacSHA1(data, hmacSHA1Key);
    }

    protected boolean isValidSessionData(SessionData sessionData) {
        String checksum = sessionData.get(CHECKSUM_KEY);
        sessionData.remove(CHECKSUM_KEY);
        return checksum.equals(checksumSessionData(sessionData));
    }

}
