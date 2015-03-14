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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Decebal Suiu
 */
public class MemorySessionDataStorage implements SessionDataStorage {

    private final Map<String, SessionData> sessions;

    public MemorySessionDataStorage() {
        sessions = new ConcurrentHashMap<>();
    }

    @Override
    public SessionData create() {
//        System.out.println("MemorySessionDataStorage.create");
//        System.out.println("sessions.size() = " + sessions.size());
        return new SessionData();
    }

    @Override
    public void save(SessionData sessionData) {
//        System.out.println("MemorySessionDataStorage.save");
//        System.out.println("sessionData = " + sessionData);
        sessions.put(sessionData.getId(), sessionData);
    }

    @Override
    public SessionData get(String sessionId) {
//        System.out.println("MemorySessionDataStorage.get");
//        System.out.println("sessionId = " + sessionId);
        SessionData sessionData = sessions.get(sessionId);
//        System.out.println("sessionData = " + sessionData);
        if (sessionData == null) {
            return null;
        }

        if (sessionData.isExpired()) {
            delete(sessionId);

            return null;
        }

        sessionData.setLastAccessedTime(System.currentTimeMillis());

        return sessionData;
    }

    @Override
    public void delete(String sessionId) {
//        System.out.println("MemorySessionDataStorage.delete");
        sessions.remove(sessionId);
    }

}
