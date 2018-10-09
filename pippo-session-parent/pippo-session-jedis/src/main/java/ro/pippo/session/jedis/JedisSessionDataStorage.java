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
package ro.pippo.session.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import ro.pippo.session.*;

/**
 * SessionDataStorage implementation with Jedis.
 *
 * @author Herman Barrantes
 */
public class JedisSessionDataStorage implements SessionDataStorage {

    private static final int IDLE_TIME = DefaultSessionData.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;
    private final JedisPool sessions;
    private final int idleTime;
    private final SessionDataTranscoder transcoder;

    /**
     * Manage session with Jedis and 30 minutes idle time.
     *
     * @param jedisPool Jedis pool
     */
    public JedisSessionDataStorage(JedisPool jedisPool) {
        this(jedisPool, IDLE_TIME, new SerializationSessionDataTranscoder());
    }

    /**
     * Manage session with Jedis in the specified idle time.
     *
     * @param jedisPool Jedis pool
     * @param idleTime idle time of the session in seconds
     */
    public JedisSessionDataStorage(JedisPool jedisPool, int idleTime) {
        this(jedisPool, idleTime, new SerializationSessionDataTranscoder());
    }

    /**
     * Manage session with Jedis in the specified idle time and transcoder
     * indicated.
     *
     * @param jedisPool Jedis pool
     * @param idleTime idle time of the session in seconds
     * @param transcoder trancoder
     */
    public JedisSessionDataStorage(JedisPool jedisPool, int idleTime, SessionDataTranscoder transcoder) {
        this.sessions = jedisPool;
        this.idleTime = idleTime;
        this.transcoder = transcoder;
    }

    @Override
    public SessionData create() {
        return new DefaultSessionData();
    }

    @Override
    public void save(SessionData sessionData) {
        try (Jedis jedis = sessions.getResource()) {
            jedis.setex(
                    sessionData.getId(),
                    idleTime,
                    transcoder.encode(sessionData));
        }
    }

    @Override
    public SessionData get(String sessionId) {
        try (Jedis jedis = sessions.getResource()) {
            String sessionStored = jedis.get(sessionId);
            if (sessionStored == null) {
                return null;
            }
            jedis.expire(sessionId, idleTime);
            SessionData sessionData = transcoder.decode(sessionStored);
            return sessionData;
        }
    }

    @Override
    public void delete(String sessionId) {
        try (Jedis jedis = sessions.getResource()) {
            jedis.del(sessionId);
        }
    }

}
