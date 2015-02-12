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

import ro.pippo.core.Request;

import javax.servlet.http.HttpSession;

/**
 * @author Decebal Suiu
 */
public class DefaultSessionFactory implements SessionFactory {

    @Override
    public Session getSession(Request request, boolean create) {
        HttpSession httpSession = request.getHttpServletRequest().getSession(create);
        if (httpSession == null) {
            // without a servlet session we can not have a DefaultSession
            return null;
        }

        return new DefaultSession(httpSession);
    }

}
