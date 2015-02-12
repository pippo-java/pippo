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
package ro.pippo.core.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Flash;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.Request;
import ro.pippo.core.util.CryptoUtils;
import ro.pippo.core.util.DefaultEncryptor;
import ro.pippo.core.util.Encryptor;
import ro.pippo.core.util.MapUtils;

import javax.servlet.http.Cookie;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Cookie Session enables pippo applications to store session data in http cookies
 * between requests instead of in memory on the server.
 * This allows application deployments to be more stateless which supports simplified
 * scaling architectures and fault tolerance.
 *
 * @author Decebal Suiu
 */
public class CookieSession implements Session {

    private static final Logger log = LoggerFactory.getLogger(CookieSession.class);

    private static final String CHECKSUM_KEY = "_cs";
    private static final String ID_KEY = "_id";
    private static final String CREATION_TIME_KEY = "_ct";

    private Request request;
    private Settings settings;

    private String id;
    private long creationTime;
    private boolean newSession;
    private Map<String, String> attributes;

    public CookieSession(Request request, boolean create, PippoSettings pippoSettings) {
        this.request = request;
        this.settings = new Settings(pippoSettings);

        attributes = new HashMap<>();

        try{
            Cookie sessionCookie = getSessionCookie();
            if (sessionCookie == null){
                initNewSession();
            } else{
                String cookieValue = sessionCookie.getValue();

                String secretKey = settings.getSecretKey();
                if (secretKey != null) {
                    cookieValue = settings.getEncryptor().decrypt(secretKey, cookieValue);
                }

                if (isValidSessionCookieValue(cookieValue)) {
                    attributes.putAll(stringToMap(cookieValue));
                    id = attributes.get(ID_KEY);
                    creationTime = Long.valueOf(attributes.get(CREATION_TIME_KEY));

                    attributes.remove(CHECKSUM_KEY);
                    attributes.remove(ID_KEY);
                    attributes.remove(CREATION_TIME_KEY);
                } else {
                    initNewSession();
                }
            }
        } catch (Exception e){
            log.error(e.getMessage(), e);
            initNewSession();
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void put(String name, Object value) {
        if (value instanceof String){
            attributes.put(name, value.toString());
            flush();
        } else {
            throw new IllegalArgumentException("Cookie session only accept String value");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T remove(String name) {
        T value = (T) attributes.remove(name);
        flush();

        return value;
    }

    @Override
    public void invalidate() {
        Cookie cookie = createCookie();
        cookie.setMaxAge(0);
        // TODO
//        response.addCookie(cookie);
    }

    @Override
<<<<<<< HEAD
    public void touch() {
        // TODO
    }

    @Override
=======
>>>>>>> d47ba05... I began working on CookieSession
    public Flash getFlash() {
        Flash flash = get("flash");
        if (flash == null) {
            put("flash", flash = new Flash());
        }

        return flash;
    }

    @Override
    public boolean isNew() {
        return newSession;
    }

    protected String generateSessionId(){
        String uuid = UUID.randomUUID().toString();
        String id = uuid.replaceAll("-", "");
        id = new BigInteger(id, 16).toString(32);

        return id;
    }

    protected boolean isValidSessionCookieValue(String cookieValue){
        boolean valid = false;

        Map<String, String> map = stringToMap(cookieValue);
        if (map.containsKey(CHECKSUM_KEY) && map.containsKey(ID_KEY) && map.containsKey(CREATION_TIME_KEY)) {
            String checksum = map.get(CHECKSUM_KEY);
            map.remove(CHECKSUM_KEY);

            valid = checksum.equals(mapChecksum(map));
        }

        return valid;
    }

    protected String mapToString(Map<String, String> map){
        return MapUtils.toString(map);
    }

    protected Map<String, String> stringToMap(String input){
        return MapUtils.fromString(input);
    }

    protected String mapChecksum(Map<String, String> map){
        try {
            return CryptoUtils.getHmacSha1(settings.getHmacSHA1Key(), mapToString(map));
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    private void initNewSession(){
        attributes.clear();
        id = generateSessionId();
        creationTime = System.currentTimeMillis();
        newSession = true;
    }

    private Cookie getSessionCookie(){
        return request.getCookie(settings.getSessionName());
    }

    private Cookie createCookie() {
        Cookie cookie;
        try{
            Map<String, String> map = new HashMap<>();
            map.putAll(attributes);
            map.put(ID_KEY, id);
            map.put(CREATION_TIME_KEY, Long.toString(creationTime));

            String checksum = mapChecksum(map);
            map.put(CHECKSUM_KEY, checksum);

            String cookieValue = mapToString(map);
            String secretKey = settings.getSecretKey();
            if (secretKey != null) {
                cookieValue = settings.getEncryptor().encrypt(secretKey, cookieValue);
            }

            cookie = new Cookie(settings.getSessionName(), cookieValue);
            cookie.setMaxAge(settings.getSessionMaxAge());
            cookie.setPath(settings.getPath());

            if (settings.getDomain() != null) {
                cookie.setDomain(settings.getDomain());
            }
        } catch (Exception e){
            throw new IllegalStateException(e);
        }

        return cookie;
    }

    private void flush(){
        // TODO
//        response.addCookie(createCookie());
    }

    public static class Settings {

        private PippoSettings pippoSettings;

        public Settings(PippoSettings pippoSettings) {
            this.pippoSettings = pippoSettings;
        }

        public String getHmacSHA1Key() {
            return pippoSettings.getRequiredString("session.hmacSHA1Key");
        }

        public String getSecretKey() {
            return pippoSettings.getString("session.secretKey", null);
        }

        public Encryptor getEncryptor() {
            // TODO cache ?! (field in class)
            Encryptor encryptor;

            String className = pippoSettings.getString("session.encryptorClass", DefaultEncryptor.class.getName());
            try {
                encryptor = (Encryptor) Class.forName(className).newInstance();
                /*
                // ii it ok from performance point of view ?!
                if (!isValidEncryptor(encryptor, getSecretKey())) {
                    throw new IllegalStateException("Not a valid encryptor");
                }
                */
            } catch (Exception e) {
                throw new PippoRuntimeException("Cannot create the encryptor", e);
            }

            return encryptor;
        }

        public String getSessionName() {
            String defaultValue = pippoSettings.getString(PippoConstants.SETTING_APPLICATION_COOKIE_PREFIX, "PIPPO")
                + "_SESSION";

            return pippoSettings.getString("session.sessionName", defaultValue);
        }

        public int getSessionMaxAge() {
            return pippoSettings.getInteger("session.sessionMaxAge", -1);
        }

        public String getPath() {
            return pippoSettings.getString("session.path", "/");
        }

        public String getDomain() {
            return pippoSettings.getString("session.domain", null);
        }

        /*
        private boolean isValidEncryptor(Encryptor encryptor, String secretKey) throws Exception {
            String data = "pippo";

            String encrypted = encryptor.encrypt(secretKey, data);
            boolean isOK = encryptor.decrypt(secretKey, encrypted).equals(data);

            return isOK;
        }
        */

    }

}
