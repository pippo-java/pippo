/*
 * Copyright (C) 2014 the original author or authors.
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
package ro.pippo.core;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Decebal Suiu
 */
public enum RuntimeMode {

    DEV("dev"), // development
    TEST("test"), // testing
    PROD("prod"); // production

    private static RuntimeMode current;

    private final String name;

    private static final Map<String, RuntimeMode> map = new HashMap<>();

    static {
        for (RuntimeMode mode : RuntimeMode.values()) {
            map.put(mode.name, mode);
        }
    }

    private RuntimeMode(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static RuntimeMode byName(String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }

        throw new NoSuchElementException("Cannot found Pippo runtime mode with name '" + name +
            "'. Must be 'dev', 'test', or 'prod'.");
    }

    public static RuntimeMode getCurrent() {
        if (current == null) {
            // retrieves the runtime mode from system
            String modeAsString = System.getProperty(PippoConstants.SYSTEM_PROPERTY_PIPPO_MODE, RuntimeMode.PROD.toString());
            current = RuntimeMode.byName(modeAsString);
        }

        return current;
    }

}
