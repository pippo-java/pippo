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

import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.util.CookieUtils;
import ro.pippo.session.SerializationSessionDataTranscoder;
import ro.pippo.session.SessionData;
import ro.pippo.session.DefaultSessionData;
import ro.pippo.session.SessionDataStorage;
import ro.pippo.session.SessionDataTranscoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Decebal Suiu
 */
public class CookieSessionDataStorage implements SessionDataStorage {

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
        return new DefaultSessionData();
    }

    @Override
    public void save(SessionData sessionData) {
        String data = transcoder.encode(sessionData);
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

        return transcoder.decode(cookie.getValue());
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

        private static final String NAME = "session.cookie.name";
        private static final String MAX_AGE = "session.cookie.maxAge";
        private static final String PATH = "session.cookie.path";
        private static final String DOMAIN = "session.cookie.domain";

        private final PippoSettings pippoSettings;

        public Settings(PippoSettings pippoSettings) {
            this.pippoSettings = pippoSettings;
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

    }

}
