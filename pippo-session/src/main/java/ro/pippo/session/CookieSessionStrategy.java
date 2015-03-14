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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Decebal Suiu
 */
public class CookieSessionStrategy implements SessionStrategy {

    public static final String SESSION_ID_COOKIE_NAME = "SESSIONID";

    @Override
    public String getRequestedSessionId(HttpServletRequest request) {
//        System.out.println("### CookieSessionStrategy.getRequestedSessionId");
        Cookie cookie = getSessionIdCookie(request);
//        System.out.println("cookie = " + cookie);

        return (cookie != null) ? cookie.getValue() : null;
    }

    @Override
    public void onNewSession(HttpServletRequest request, HttpServletResponse response, SessionData sessionData) {
//        System.out.println("CookieSessionStrategy.onNewSession");
        String sessionId = sessionData.getId();
//        System.out.println("sessionId = " + sessionId);
        Cookie cookie = createSessionIdCookie(request, sessionId);
//        System.out.println("cookie = " + cookie);
        response.addCookie(cookie);
    }

    @Override
    public void onInvalidatedSession(HttpServletRequest request, HttpServletResponse response) {
//        System.out.println("CookieSessionStrategy.onInvalidatedSession");
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
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SESSION_ID_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }

        return null;
    }

}
