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

/**
 * @author Decebal Suiu
 */
public class SessionManager {

    private SessionDataStorage sessionDataStorage;
    private SessionStrategy sessionStrategy;

    public SessionManager() {
        this(new MemorySessionDataStorage());
    }

    public SessionManager(SessionDataStorage sessionDataStorage) {
        this(sessionDataStorage, new CookieSessionStrategy());
    }

    public SessionManager(SessionDataStorage sessionDataStorage, SessionStrategy sessionStrategy) {
        this.sessionDataStorage = sessionDataStorage;
        this.sessionStrategy = sessionStrategy;
    }

    public SessionDataStorage getSessionDataStorage() {
        return sessionDataStorage;
    }

    public SessionStrategy getSessionStrategy() {
        return sessionStrategy;
    }

}
