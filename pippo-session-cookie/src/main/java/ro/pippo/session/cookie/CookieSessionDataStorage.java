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

import ro.pippo.session.SerializationSessionDataTranscoder;
import ro.pippo.session.SessionDataTranscoder;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.util.CookieUtils;
import ro.pippo.session.SessionData;
import ro.pippo.session.SessionDataStorage;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Decebal Suiu
 */
public class CookieSessionDataStorage implements SessionDataStorage {

    public static final String SESSION_COOKIE_NAME = "SESSION";

    private final SessionDataTranscoder transcoder;

    public CookieSessionDataStorage() {
        this(new SerializationSessionDataTranscoder());
    }

    public CookieSessionDataStorage(SessionDataTranscoder transcoder) {
        this.transcoder = transcoder;
    }

    @Override
    public SessionData create() {
        return new SessionData();
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
//        System.out.println("cookie = " + cookie);

        if (cookie == null) {
            // TODO or a new SessionData with and an warning/error in log ?!
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

    private Cookie createSessionCookie(HttpServletRequest request, String data) {
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, data);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(request.getContextPath() + "/");
        // TODO setDomain

        return cookie;
    }

    private HttpServletResponse getHttpServletResponse() {
        return Response.get().getHttpServletResponse();
    }

    private Cookie getSessionCookie(HttpServletRequest request) {
        return CookieUtils.getCookie(request, SESSION_COOKIE_NAME);
    }

    private HttpServletRequest getHttpServletRequest() {
        return Request.get().getHttpServletRequest();
    }

}
