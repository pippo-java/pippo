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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author Decebal Suiu
 */
class PippoHttpSession implements HttpSession {

    private final SessionData sessionData;
    private final ServletContext servletContext;

    private boolean invalidated;
    private boolean old;

    public PippoHttpSession(SessionData sessionData, ServletContext servletContext) {
        this.sessionData = sessionData;
        this.servletContext = servletContext;
    }

    public SessionData getSessionData() {
        return sessionData;
    }

    @Override
    public String getId() {
        return sessionData.getId();
    }

    @Override
    public long getCreationTime() {
        checkInvalidated();

        return sessionData.getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        checkInvalidated();

        return sessionData.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        sessionData.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        checkInvalidated();

        return sessionData.getMaxInactiveInterval();
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        checkInvalidated();

        return sessionData.get(name);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkInvalidated();

        return Collections.enumeration(sessionData.getNames());
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public String[] getValueNames() {
        checkInvalidated();

        return sessionData.getNames().toArray(new String[0]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkInvalidated();

        sessionData.put(name, value);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        checkInvalidated();

        sessionData.remove(name);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Override
    public void invalidate() {
        checkInvalidated();

        this.invalidated = true;

        onInvalidate();
    }

    protected void onInvalidate() {
    }

    @Override
    public boolean isNew() {
        checkInvalidated();

        return !old;
    }

    public void setNew(boolean isNew) {
        this.old = !isNew;
    }

    private void checkInvalidated() {
        if (invalidated) {
            throw new IllegalStateException("The HttpSession has already be invalidated.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PippoHttpSession that = (PippoHttpSession) o;

        return sessionData.equals(that.sessionData);
    }

    @Override
    public int hashCode() {
        return sessionData.hashCode();
    }

    @Override
    public String toString() {
        return "PippoHttpSession{" +
            "sessionData=" + sessionData +
            '}';
    }

}
