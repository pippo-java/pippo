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
package ro.pippo.session.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import ro.pippo.session.DefaultSessionData;
import ro.pippo.session.SessionData;
import ro.pippo.session.SessionDataStorage;

/**
 * SessionDataStorage implementation with Hazelcast.
 *
 * @author Herman Barrantes
 */
public class HazelcastSessionDataStorage implements SessionDataStorage {

    private static final String SESSION_NAME = "session";
    private final IMap<String, SessionData> sessions;

    /**
     * Manage session with the map named "session" and Hazelcast specified
     * instance.
     *
     * @param hazelcastInstance hazelcast instance
     * @see #HazelcastSessionDataStorage(java.lang.String,
     * com.hazelcast.core.HazelcastInstance)
     */
    public HazelcastSessionDataStorage(HazelcastInstance hazelcastInstance) {
        this(SESSION_NAME, hazelcastInstance);
    }

    /**
     * Management session with the map specified in the name and Hazelcast
     * specified instance.
     *
     * @param name name of cache
     * @param hazelcastInstance hazelcast instance
     */
    public HazelcastSessionDataStorage(String name, HazelcastInstance hazelcastInstance) {
        this.sessions = hazelcastInstance.getMap(name);
    }

    /**
     * Manage session with custom Hazelcast IMap for sessions.
     *
     * @param sessions Hazelcast IMap for sessions.
     */
    public HazelcastSessionDataStorage(IMap<String, SessionData> sessions) {
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
}
