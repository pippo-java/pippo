package ro.pippo.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DefaultSessionData implements SessionData
{
    private String id;
    private Map<String, Object> attributes;

    private long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval;

    public DefaultSessionData() {
        id = UUID.randomUUID().toString().replace( "-", "");
        attributes = new HashMap<>();
        creationTime = lastAccessedTime = System.currentTimeMillis();
        maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;
    }

    @Override
    public String getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public Set<String> getNames() {
        return attributes.keySet();
    }

    @Override
    public void put(String name, Object value) {
        if (value == null) {
            remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    @Override
    public <T> T remove(String name) {
        T t = get(name);
        attributes.remove(name);

        return t;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    boolean isExpired(long now) {
        if (maxInactiveInterval < 0) {
            return false;
        }

        return now - TimeUnit.SECONDS.toMillis( maxInactiveInterval) >= lastAccessedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionData that = (SessionData) o;

        return id.equals(that.getId());
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
