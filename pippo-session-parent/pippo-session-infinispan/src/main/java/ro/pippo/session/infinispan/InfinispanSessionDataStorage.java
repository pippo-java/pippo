/*
 * Copyright 2016 the original author or authors.
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
import org.infinispan.manager.EmbeddedCacheManager;
import ro.pippo.session.DefaultSessionData;
import ro.pippo.session.SessionData;
import ro.pippo.session.SessionDataStorage;

/**
 * SessionDataStorage implementation with Infinispan.
 *
 * @author Herman Barrantes
 */
public class InfinispanSessionDataStorage implements SessionDataStorage {

    private static final String SESSION_NAME = "session";
    private final Cache<String, SessionData> sessions;

    /**
     * Manage session with the cache named "session" and CacheManager specified.
     *
     * @param cacheManager cache manager
     */
    public InfinispanSessionDataStorage(EmbeddedCacheManager cacheManager) {
        this(cacheManager, SESSION_NAME);
    }

    /**
     * Manage session with the cache specified and CacheManager specified.
     *
     * @param cacheManager cache manager
     * @param cacheName cache name
     */
    public InfinispanSessionDataStorage(EmbeddedCacheManager cacheManager, String cacheName) {
        this.sessions = cacheManager.getCache(cacheName);
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
        return new DefaultSessionData();
    }

    @Override
    public void save(SessionData sessionData) {
        this.sessions.put(sessionData.getId(), sessionData);
    }

    @Override
    public SessionData get(String sessionId) {
        return this.sessions.get(sessionId);
    }

    @Override
    public void delete(String sessionId) {
        this.sessions.remove(sessionId);
    }

}
