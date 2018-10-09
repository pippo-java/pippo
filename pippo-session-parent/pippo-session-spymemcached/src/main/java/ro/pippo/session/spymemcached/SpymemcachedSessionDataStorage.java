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
package ro.pippo.session.spymemcached;

import net.spy.memcached.MemcachedClient;
import ro.pippo.session.DefaultSessionData;
import ro.pippo.session.SessionData;
import ro.pippo.session.SessionDataStorage;

/**
 * SessionDataStorage implementation with Spymemcached.
 *
 * @author Herman Barrantes
 */
public class SpymemcachedSessionDataStorage implements SessionDataStorage {

    private static final int IDLE_TIME = DefaultSessionData.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;
    private final MemcachedClient sessions;
    private final int idleTime;

    /**
     * Manage session with a SpyMemcached client and 30 minutes idle time.
     *
     * @param client memcached client
     * @see #SpymemcachedSessionDataStorage(net.spy.memcached.MemcachedClient,
     * int)
     */
    public SpymemcachedSessionDataStorage(final MemcachedClient client) {
        this(client, IDLE_TIME);
    }

    /**
     * Manage session with a SpyMemcached client and custom idle time in
     * seconds.
     *
     * @param client memcached client
     * @param idleTime idle time of the session in seconds
     */
    public SpymemcachedSessionDataStorage(final MemcachedClient client, int idleTime) {
        this.sessions = client;
        this.idleTime = idleTime;
    }

    @Override
    public SessionData create() {
        return new DefaultSessionData();
    }

    @Override
    public void save(SessionData sessionData) {
        this.sessions.set(sessionData.getId(), idleTime, sessionData);
    }

    @Override
    public SessionData get(String sessionId) {
        return (SessionData) this.sessions.get(sessionId);
    }

    @Override
    public void delete(String sessionId) {
        this.sessions.delete(sessionId);
    }

}
