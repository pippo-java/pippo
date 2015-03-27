/*
 * Copyright (C) 2014 the original author or authors.
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
package ro.pippo.core;

import ro.pippo.core.Flash;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * @author Decebal Suiu
 */
public class Session {

    private HttpSession httpSession;

    public Session(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public String getId() {
        return httpSession.getId();
    }

    public void put(String name, Object value) {
        httpSession.setAttribute(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) httpSession.getAttribute(name);
    }

    public Enumeration<String> getNames() {
        return httpSession.getAttributeNames();
    }

    public <T> T remove(String name) {
        T t = get(name);
        httpSession.removeAttribute(name);

        return t;
    }

    public void invalidate() {
        httpSession.invalidate();
    }

    public void touch() {
        // modify the session to keep it alive
        put("__touch", "DOES_NOT_MATTER");
        remove("__touch");
    }

    public Flash getFlash() {
        Flash flash = get("flash");
        if (flash == null) {
            put("flash", flash = new Flash());
        }

        return flash;
    }

    public boolean isNew() {
        return httpSession.isNew();
    }

    public HttpSession getHttpSession() {
        return httpSession;
    }

}
