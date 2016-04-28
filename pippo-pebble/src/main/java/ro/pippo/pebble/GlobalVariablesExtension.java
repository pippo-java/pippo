/*
 * Copyright (C) 2016 the original author or authors.
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
package ro.pippo.pebble;

import com.mitchellbosecke.pebble.extension.AbstractExtension;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalVariablesExtension extends AbstractExtension {

    private final Map<String, Object> globalVariables;

    public GlobalVariablesExtension() {
        this(Collections.emptyMap());
    }

    public GlobalVariablesExtension(Map<String, Object> variables) {
        this.globalVariables = new ConcurrentHashMap<>(variables);
    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public GlobalVariablesExtension set(String key, Object value) {
        put(key, value);

        return this;
    }

    public void put(String key, Object value) {
        globalVariables.put(key, value);
    }

    public Object remove(String key) {
        return globalVariables.remove(key);
    }

    public <X> X get(String key) {
        return (X) globalVariables.get(key);
    }

}
