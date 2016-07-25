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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Persistable data about a session.
 * @see SessionDataStorage
 *
 * @author Decebal Suiu
 */
public class SessionData implements Serializable {

    public static final int DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS = 30 * 60; // 30 minutes

    private String id;
    private Map<String, Object> attributes;

    private long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval;

    public SessionData() {
        id = UUID.randomUUID().toString().replace("-", "");
        attributes = new HashMap<>();
        creationTime = lastAccessedTime = System.currentTimeMillis();
        maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;
    }

    public String getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) attributes.get(name);
    }

    public Set<String> getNames() {
        return attributes.keySet();
    }

    public void put(String name, Object value) {
        if (value == null) {
            remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    public <T> T remove(String name) {
        T t = get(name);
        attributes.remove(name);

        return t;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    boolean isExpired(long now) {
        if (maxInactiveInterval < 0) {
            return false;
        }

        return now - TimeUnit.SECONDS.toMillis(maxInactiveInterval) >= lastAccessedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionData that = (SessionData) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "SessionData{" +
            "id='" + id + '\'' +
            ", creationTime=" + creationTime +
            ", lastAccessedTime=" + lastAccessedTime +
            ", maxInactiveInterval=" + maxInactiveInterval +
            ", attributes=" + attributes +
            '}';
    }

}
