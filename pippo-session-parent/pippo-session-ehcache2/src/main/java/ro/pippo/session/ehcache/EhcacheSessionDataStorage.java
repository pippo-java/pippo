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
package ro.pippo.session.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import ro.pippo.session.DefaultSessionData;
import ro.pippo.session.SessionData;
import ro.pippo.session.SessionDataStorage;

/**
 * SessionDataStorage implementation with Ehcache2.
 *
 * @author Herman Barrantes
 */
public class EhcacheSessionDataStorage implements SessionDataStorage {

    private static final String SESSION_NAME = "session";
    private final Cache sessions;

    /**
     * Manage session with the cache named "session".
     *
     * @see #EhcacheSessionDataStorage(java.lang.String)
     */
    public EhcacheSessionDataStorage() {
        this(SESSION_NAME);
    }

    /**
     * Manage session with the cache specified in the name.
     *
     * @param name name of cache
     */
    public EhcacheSessionDataStorage(String name) {
        this.sessions = CacheManager.getInstance().getCache(name);
    }

    /**
     * Manage session with custom cache.
     *
     * @param cache custom cache.
     */
    public EhcacheSessionDataStorage(Cache cache) {
        this.sessions = cache;
    }

    @Override
    public SessionData create() {
        return new DefaultSessionData();
    }

    @Override
    public void save(SessionData sessionData) {
        this.sessions.put(new Element(sessionData.getId(), sessionData));
    }

    @Override
    public SessionData get(String sessionId) {
        Element element = this.sessions.get(sessionId);
        return element == null ? null : (SessionData) element.getObjectValue();
    }

    @Override
    public void delete(String sessionId) {
        this.sessions.remove(sessionId);
    }

}
