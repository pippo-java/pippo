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
package ro.pippo.session.jcache;

import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import ro.pippo.session.DefaultSessionData;
import ro.pippo.session.SessionData;
import ro.pippo.session.SessionDataStorage;

/**
 * SessionDataStorage implementation with JCache.
 *
 * @author Herman Barrantes
 */
public class JCacheSessionDataStorage implements SessionDataStorage {

    private static final String SESSION_NAME = "session";
    private static final long IDLE_TIME = DefaultSessionData.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;
    private final Cache<String, SessionData> sessions;

    /**
     * Manage session with the cache named "session" and 30 minutes idle time.
     *
     * @see #JCacheSessionDataStorage(java.lang.String, long)
     */
    public JCacheSessionDataStorage() {
        this(SESSION_NAME, IDLE_TIME);
    }

    /**
     * Manage session with the cache specified in the name and 30 minutes idle
     * time.
     *
     * @param name cache name
     */
    public JCacheSessionDataStorage(String name) {
        this(name, IDLE_TIME);
    }

    /**
     * Manage session with the cache named "session" and custom idle time in
     * seconds.
     *
     * @param idleTime idle time in seconds
     */
    public JCacheSessionDataStorage(long idleTime) {
        this(SESSION_NAME, idleTime);
    }

    /**
     * Manage session with the cache specified in the name and custom idle time
     * in seconds.
     *
     * @param name cache name
     * @param idleTime idle time in seconds
     */
    public JCacheSessionDataStorage(String name, long idleTime) {
        this.sessions = create(name, idleTime);
    }

    public JCacheSessionDataStorage(Cache<String, SessionData> sessions) {
        this.sessions = sessions;
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

    /**
     * Create a cache with name and expiry policy with idle time.
     *
     * @param name name of cache
     * @param idleTime idle time in seconds
     * @return the cache with name and expiry policy with idle time
     */
    protected Cache<String, SessionData> create(String name, long idleTime) {
        return Caching
                .getCachingProvider()
                .getCacheManager()
                .createCache(
                        name,
                        new MutableConfiguration<String, SessionData>()
                        .setTypes(String.class, SessionData.class)
                        .setExpiryPolicyFactory(
                                TouchedExpiryPolicy.factoryOf(
                                        new Duration(
                                                TimeUnit.SECONDS,
                                                idleTime))));
    }

}
