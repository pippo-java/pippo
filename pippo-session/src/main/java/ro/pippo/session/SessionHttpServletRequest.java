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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Decebal Suiu
 */
public class SessionHttpServletRequest extends HttpServletRequestWrapper {

    private final SessionManager sessionManager;

    private PippoHttpSession currentSession;
    private Boolean requestedSessionIdValid;

    public SessionHttpServletRequest(HttpServletRequest request, SessionManager sessionManager) {
        super(request);

        this.sessionManager = sessionManager;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (currentSession != null) {
            return currentSession;
        }

        String requestedSessionId = getRequestedSessionId();
        if (requestedSessionId != null) {
            SessionData session = getSessionDataStorage().get(requestedSessionId);
            if (session != null) {
                requestedSessionIdValid = true;
                currentSession = createSession(session);
                currentSession.setNew(false);

                return currentSession;
            }
        }

        if (!create) {
            return null;
        }

        SessionData session = getSessionDataStorage().create();
        currentSession = createSession(session);

        return currentSession;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public String getRequestedSessionId() {
        return getSessionStrategy().getRequestedSessionId(this);
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        if (requestedSessionIdValid == null) {
            String sessionId = getRequestedSessionId();
            SessionData sessionData = (sessionId != null) ? getSessionDataStorage().get(sessionId) : null;

            return isRequestedSessionIdValid(sessionData);
        }

        return super.isRequestedSessionIdValid();
    }

    private SessionDataStorage getSessionDataStorage() {
        return sessionManager.getSessionDataStorage();
    }

    private SessionStrategy getSessionStrategy() {
        return sessionManager.getSessionStrategy();
    }

    private boolean isRequestedSessionIdValid(SessionData sessionData) {
        if (requestedSessionIdValid == null) {
            requestedSessionIdValid = (sessionData != null);
        }

        return requestedSessionIdValid;
    }

    private PippoHttpSession createSession(SessionData sessionData) {
        return new PippoHttpSession(sessionData, getServletContext()) {

            @Override
            public void onInvalidate() {
                currentSession = null;
                getSessionDataStorage().delete(getId());
            }

        };
    }

    private boolean isInvalidateClientSession() {
        return (currentSession == null) && isRequestedSessionIdValid();
    }

    public void commitSession(HttpServletResponse response) {
//        System.out.println("SessionHttpServletRequest.commitSession");
        PippoHttpSession wrappedSession = currentSession;
        if (wrappedSession == null) {
            if (isInvalidateClientSession()) {
                getSessionStrategy().onInvalidatedSession(this, response);
            }
        } else {
            SessionData sessionData = wrappedSession.getSessionData();
            getSessionDataStorage().save(sessionData);
            if (!isRequestedSessionIdValid() || !sessionData.getId().equals(getRequestedSessionId())) {
                getSessionStrategy().onNewSession(this, response, sessionData);
            }
        }
    }

}
