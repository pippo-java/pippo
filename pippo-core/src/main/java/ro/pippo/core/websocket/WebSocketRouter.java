/*
 * Copyright (C) 2017 the original author or authors.
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
package ro.pippo.core.websocket;

import ro.pippo.core.DefaultUriMatcher;
import ro.pippo.core.UriMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class WebSocketRouter {

    // key = uri pattern
    private Map<String, WebSocketHandler> handlers;

    private UriMatcher uriMatcher;

    public WebSocketRouter() {
        handlers = new HashMap<>();
        uriMatcher = new DefaultUriMatcher();
    }

    public WebSocketMatch match(String requestUri) {
        for (String uriPattern : handlers.keySet()) {
            Map<String, String> pathParameters = uriMatcher.match(requestUri, uriPattern);
            if (pathParameters != null) {
                return new WebSocketMatch(uriPattern, handlers.get(uriPattern), pathParameters);
            }
        }

        // TODO
        return null;
    }

    public void addRoute(String uriPattern, WebSocketHandler handler) {
        handlers.put(uriPattern, handler);
        uriMatcher.addUriPattern(uriPattern);
    }

    public static class WebSocketMatch {

        private final String uriPattern;
        private final WebSocketHandler handler;
        private final Map<String, String> pathParameters;

        public WebSocketMatch(String uriPattern, WebSocketHandler handler, Map<String, String> pathParameters) {
            this.uriPattern = uriPattern;
            this.handler = handler;
            this.pathParameters = pathParameters;
        }

        public String getUriPattern() {
            return uriPattern;
        }

        public WebSocketHandler getHandler() {
            return handler;
        }

        public Map<String, String> getPathParameters() {
            return pathParameters;
        }

    }

}
