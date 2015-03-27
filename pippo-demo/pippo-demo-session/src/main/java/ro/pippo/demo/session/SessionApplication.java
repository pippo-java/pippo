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
package ro.pippo.demo.session;

import ro.pippo.core.Application;
import ro.pippo.core.RequestResponseFactory;
import ro.pippo.core.Session;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.session.SessionDataStorage;
import ro.pippo.session.SessionManager;
import ro.pippo.session.SessionRequestResponseFactory;
import ro.pippo.session.cookie.CookieSessionDataStorage;

import java.util.Enumeration;

/**
 * @author Decebal Suiu
 */
public class SessionApplication extends Application {

    @Override
    protected void onInit() {
        GET("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                StringBuilder builder = new StringBuilder();
                builder.append("Request: " + routeContext.getRequest().getHttpServletRequest());
                builder.append("<br>");
                Session session = routeContext.getRequest().getSession(false);
                builder.append("Session: " + ((session != null) ? session.getId() : "<a href=\"session\">Create</a>"));
                builder.append("<br>");

                if (session != null) {
                    // show attributes
                    Enumeration<String> names = session.getNames();
                    while (names.hasMoreElements()) {
                        String name = names.nextElement();
                        builder.append("&nbsp;&nbsp;&nbsp;" + name + " > " + session.get(name));
                        builder.append("<br>");
                    }
                }

                routeContext.send(builder);
            }

        });

        GET("/session", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
//                routeContext.flashError("Test 123");
                routeContext.setSession("test", "a simple test");
                routeContext.redirect("/");
            }

        });
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new CookieSessionDataStorage();
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }

}
