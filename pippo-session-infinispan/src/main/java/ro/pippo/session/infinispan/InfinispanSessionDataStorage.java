/*
 * Copyright 2016 Herman Barrantes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.session.infinispan;

import org.infinispan.Cache;
import ro.pippo.session.SessionData;
import ro.pippo.session.SessionDataStorage;

/**
 *
 * @author Herman Barrantes
 * @since 06/06/2016
 */
public class InfinispanSessionDataStorage implements SessionDataStorage {

    private static final String SESSION_NAME = "session";
    private final Cache<String, SessionData> sessions;

    /**
     * Manage session with the cache named "session".
     *
     * @see #InfinispanSessionDataStorage(java.lang.String)
     */
    public InfinispanSessionDataStorage() {
        this(SESSION_NAME);
    }

    /**
     * Manage session with the cache specified in the name.
     *
     * @param name name of cache
     */
    public InfinispanSessionDataStorage(String name) {
        this.sessions = InfinispanSingleton.getInstance().getCache(name);
    }

    /**
     * Manage session with custom cache.
     *
     * @param cache custom cache.
     */
    public InfinispanSessionDataStorage(Cache<String, SessionData> cache) {
        this.sessions = cache;
    }

    @Override
    public SessionData create() {
        return new SessionData();
    }

    @Override
    public void save(SessionData sessionData) {
        this.sessions.put(sessionData.getId(), sessionData);
    }

    @Override
    public SessionData get(String sessionId) {
        SessionData sessionData = this.sessions.get(sessionId);
        if (sessionData == null) {
            return null;
        }
        return sessionData;
    }

    @Override
    public void delete(String sessionId) {
        this.sessions.remove(sessionId);
    }

}
