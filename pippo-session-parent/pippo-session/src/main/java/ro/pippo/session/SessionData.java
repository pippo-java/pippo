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
public interface SessionData extends Serializable {

    String getId();

    <T> T get(String name);

    Set<String> getNames();

    void put(String name, Object value);

    <T> T remove(String name);

    long getCreationTime();

    void setCreationTime(long creationTime);

    long getLastAccessedTime();

    void setLastAccessedTime(long lastAccessedTime);

    void setMaxInactiveInterval(int interval);

    int getMaxInactiveInterval();

    boolean isExpired();
}
