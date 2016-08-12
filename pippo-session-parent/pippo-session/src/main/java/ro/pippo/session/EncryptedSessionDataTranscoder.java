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
package ro.pippo.session;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.CryptoUtils;

/**
 * @author Herman Barrantes
 */
public class EncryptedSessionDataTranscoder implements SessionDataTranscoder {

    private static final String CHECKSUM_KEY = "_cs";

    private final String secretKey;
    private final String hmacSHA1Key;
    private final Encryptor encryptor;
    private final SessionDataTranscoder transcoder;

    private EncryptedSessionDataTranscoder(String secretKey, String hmacSHA1Key, SessionDataTranscoder transcoder, Encryptor encryptor) {
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

            return isValidSessionData(sessionData) ? sessionData : null;
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

    public static class Builder {

        private String secretKey;
        private String hmacSHA1Key;
        private Encryptor encryptor;
        private SessionDataTranscoder transcoder;

        public Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder hmacSHA1Key(String hmacSHA1Key) {
            this.hmacSHA1Key = hmacSHA1Key;
            return this;
        }

        public Builder encryptor(Encryptor encryptor) {
            this.encryptor = encryptor;
            return this;
        }

        public Builder transcoder(SessionDataTranscoder transcoder) {
            this.transcoder = transcoder;
            return this;
        }

        public EncryptedSessionDataTranscoder build() {
            if (secretKey == null) {
                throw new PippoRuntimeException("secretKey is required");
            }

            if (hmacSHA1Key == null) {
                hmacSHA1Key = secretKey;
            }

            if (encryptor == null) {
                encryptor = new DefaultEncryptor();
            }

            if (transcoder == null) {
                transcoder = new SerializationSessionDataTranscoder();
            }

            return new EncryptedSessionDataTranscoder(secretKey, hmacSHA1Key, transcoder, encryptor);
        }

    }

}
