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
package ro.pippo.session;

import ro.pippo.core.util.CookieUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Decebal Suiu
 */
public class CookieSessionStrategy implements SessionStrategy {

    private static final String SESSION_ID_COOKIE_NAME = "SESSIONID";

    @Override
    public String getRequestedSessionId(HttpServletRequest request) {
        Cookie cookie = getSessionIdCookie(request);
        return (cookie != null) ? cookie.getValue() : null;
    }

    @Override
    public void onNewSession(HttpServletRequest request, HttpServletResponse response, SessionData sessionData) {
        String sessionId = sessionData.getId();
        Cookie cookie = createSessionIdCookie(request, sessionId);
        response.addCookie(cookie);
    }

    @Override
    public void onInvalidatedSession(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = createSessionIdCookie(request, "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Cookie createSessionIdCookie(HttpServletRequest request, String sessionId) {
        Cookie cookie = new Cookie(SESSION_ID_COOKIE_NAME, sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(request.getContextPath() + "/");
        // TODO setDomain

        return cookie;
    }

    private Cookie getSessionIdCookie(HttpServletRequest request) {
        return CookieUtils.getCookie(request, SESSION_ID_COOKIE_NAME);
    }

}
