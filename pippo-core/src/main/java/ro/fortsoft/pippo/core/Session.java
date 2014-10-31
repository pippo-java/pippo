/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.core;

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

    public void setAttribute(String name, Object value) {
        httpSession.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        return httpSession.getAttribute(name);
    }

    public Enumeration<String> getAttributeNames() {
        return httpSession.getAttributeNames();
    }

    public void removeAttribute(String name) {
        httpSession.removeAttribute(name);
    }

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
