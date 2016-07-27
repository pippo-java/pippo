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
package ro.pippo.session.cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.util.CookieUtils;
import ro.pippo.core.util.CryptoUtils;
import ro.pippo.core.util.StringUtils;
import ro.pippo.session.SerializationSessionDataTranscoder;
import ro.pippo.session.SessionData;
import ro.pippo.session.SessionDataStorage;
import ro.pippo.session.SessionDataTranscoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Decebal Suiu
 */
public class CookieSessionDataStorage implements SessionDataStorage {

    private static final Logger log = LoggerFactory.getLogger(CookieSessionDataStorage.class);

    private static final String CHECKSUM_KEY = "_cs";

    private final Settings settings;
    private final SessionDataTranscoder transcoder;

    public CookieSessionDataStorage(PippoSettings pippoSettings) {
        this(pippoSettings, new SerializationSessionDataTranscoder());
    }

    public CookieSessionDataStorage(PippoSettings pippoSettings, SessionDataTranscoder transcoder) {
        this.transcoder = transcoder;
        this.settings = new Settings(pippoSettings);
    }

    @Override
    public SessionData create() {
        return new SessionData();
    }

    @Override
    public void save(SessionData sessionData) {
        String checksum = checksumSessionData(sessionData);
        sessionData.put(CHECKSUM_KEY, checksum);

        String data = transcoder.encode(sessionData);

        String secretKey = settings.getSecretKey();
        if (secretKey != null) {
            try {
                data = settings.getEncryptor().encrypt(data, secretKey);
            } catch (Exception e) {
                throw new PippoRuntimeException(e);
            }
        }

        Cookie cookie = createSessionCookie(getHttpServletRequest(), data);
        getHttpServletResponse().addCookie(cookie);
    }

    @Override
    public SessionData get(String sessionId) {
        Cookie cookie = getSessionCookie(getHttpServletRequest());
        if (cookie == null) {
            // TODO create a new SessionData with an warning/error in log ?!
            return null;
        }

        String cookieValue = cookie.getValue();

        String secretKey = settings.getSecretKey();
        if (secretKey != null) {
            try {
                cookieValue = settings.getEncryptor().decrypt(cookieValue, secretKey);
            } catch (Exception e) {
                throw new PippoRuntimeException(e);
            }
        }

        SessionData sessionData = transcoder.decode(cookieValue);
        if (isValidSessionData(sessionData)) {
            sessionData.remove(CHECKSUM_KEY);
        } else {
            log.error("Invalid session data");
            // TODO create a new SessionData ?!
            return null;
        }

        return sessionData;
    }

    @Override
    public void delete(String sessionId) {
        Cookie cookie = createSessionCookie(getHttpServletRequest(), "");
        cookie.setMaxAge(0);
        getHttpServletResponse().addCookie(cookie);
    }

    protected Cookie createSessionCookie(HttpServletRequest request, String data) {
        Cookie cookie = new Cookie(settings.getCookieName(), data);
//        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(settings.getMaxAge());
//        cookie.setPath(request.getContextPath() + "/");
        cookie.setPath(settings.getPath());

        if (settings.getDomain() != null) {
            cookie.setDomain(settings.getDomain());
        }

        return cookie;
    }

    protected boolean isValidSessionData(SessionData sessionData){
        boolean valid = false;

        String checksum = sessionData.get(CHECKSUM_KEY);
        if (!StringUtils.isNullOrEmpty(checksum)) {
            sessionData.remove(CHECKSUM_KEY);
            valid = checksum.equals(checksumSessionData(sessionData));
        }

        return valid;
    }

    protected String checksumSessionData(SessionData sessionData) {
        String data = transcoder.encode(sessionData);
        try {
            return CryptoUtils.getHmacSHA1(data, settings.getHmacSHA1Key());
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    private HttpServletResponse getHttpServletResponse() {
        return Response.get().getHttpServletResponse();
    }

    private Cookie getSessionCookie(HttpServletRequest request) {
        return CookieUtils.getCookie(request, settings.getCookieName());
    }

    private HttpServletRequest getHttpServletRequest() {
        return Request.get().getHttpServletRequest();
    }

    static class Settings {

        private static final String HMAC_SHA1_KEY = "session.cookie.hmacSHA1Key";
        private static final String SECRET_KEY = "session.cookie.secretKey";
        private static final String ENCRYPTOR_CLASS = "session.cookie.encryptorClass";
        private static final String NAME = "session.cookie.name";
        private static final String MAX_AGE = "session.cookie.maxAge";
        private static final String PATH = "session.cookie.path";
        private static final String DOMAIN = "session.cookie.domain";

        private PippoSettings pippoSettings;

        public Settings(PippoSettings pippoSettings) {
            this.pippoSettings = pippoSettings;
        }

        public String getHmacSHA1Key() {
            return pippoSettings.getRequiredString(HMAC_SHA1_KEY);
        }

        public String getSecretKey() {
            return pippoSettings.getString(SECRET_KEY, null);
        }

        public Encryptor getEncryptor() {
            // TODO cache ?! (field in class)
            Encryptor encryptor;

            String className = pippoSettings.getString(ENCRYPTOR_CLASS, DefaultEncryptor.class.getName());
            try {
                encryptor = (Encryptor) Class.forName(className).newInstance();
                /*
                // is it ok from performance point of view ?!
                if (!isValidEncryptor(encryptor, getSecretKey())) {
                    throw new IllegalStateException("Not a valid encryptor");
                }
                */
            } catch (Exception e) {
                throw new PippoRuntimeException("Cannot create the encryptor", e);
            }

            return encryptor;
        }

        public String getCookieName() {
            String defaultValue = pippoSettings.getString(PippoConstants.SETTING_APPLICATION_COOKIE_PREFIX, "PIPPO")
                + "_SESSION";

            return pippoSettings.getString(NAME, defaultValue);
        }

        public int getMaxAge() {
            return pippoSettings.getInteger(MAX_AGE, -1);
        }

        public String getPath() {
            return pippoSettings.getString(PATH, "/");
        }

        public String getDomain() {
            return pippoSettings.getString(DOMAIN, null);
        }

        /*
        private boolean isValidEncryptor(Encryptor encryptor, String secretKey) throws Exception {
            String data = "pippo";
            String encrypted = encryptor.encrypt(secretKey, data);

            return encryptor.decrypt(secretKey, encrypted).equals(data);
        }
        */

    }

}
