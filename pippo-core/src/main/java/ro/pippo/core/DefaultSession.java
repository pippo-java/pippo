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

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * @author Decebal Suiu
 */
public class DefaultSession implements Session {

    private HttpSession httpSession;

    public DefaultSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    public String getId() {
        return httpSession.getId();
    }

    @Override
    public void put(String name, Object value) {
        httpSession.setAttribute(name, value);
    }

    @Override
    public <T> T get(String name) {
        return (T) httpSession.getAttribute(name);
    }

    @Override
    public Enumeration<String> getKeys() {
        return httpSession.getAttributeNames();
    }

    @Override
    public void remove(String name) {
        httpSession.removeAttribute(name);
    }

    @Override
    public void invalidate() {
        httpSession.invalidate();
    }

    public boolean isNew() {
        return httpSession.isNew();
    }

    public HttpSession getHttpSession() {
        return httpSession;
    }

}
