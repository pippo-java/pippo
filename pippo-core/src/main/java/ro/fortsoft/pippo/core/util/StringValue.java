/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public class StringValue implements Serializable {

    private final String value;

    public StringValue(final String value) {
        this.value = value;
    }

    public int toInt() {
        return toInt(0);
    }

    public int toInt(int defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return Integer.parseInt(value);
    }

    public long toLong() {
        return toLong(0);
    }

    public long toLong(long defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return Long.parseLong(value);
    }

    public float toFloat() {
        return toFloat(0);
    }

    public float toFloat(float defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return Float.parseFloat(value);
    }

    public double toDouble() {
        return toDouble(0);
    }

    public double toDouble(double defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return Double.parseDouble(value);
    }

    public boolean toBoolean() {
        return toBoolean(false);
    }

    public boolean toBoolean(boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return Boolean.valueOf(value);
    }

    public Set<String> toSet() {
        return toSet(new HashSet<String>());
    }

    public Set<String> toSet(Set<String> defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return new HashSet<String>(Arrays.asList(value.split(",")));
    }

    public List<String> toList() {
        return toList(new ArrayList<String>());
    }

    public List<String> toList(List<String> defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return Arrays.asList(value.split(","));
    }

    public Object to(Class<?> type) throws Exception {
        if (type == null) {
            return null;
        }

        if (type == String.class) {
            return toString();
        }

        if ((type == Integer.TYPE) || (type == Integer.class)) {
            return toInt();
        }

        if ((type == Long.TYPE) || (type == Long.class)) {
            return toLong();
        }

        if ((type == Boolean.TYPE) || (type == Boolean.class)) {
            return toBoolean();
        }

        if ((type == Double.TYPE) || (type == Double.class)) {
            return toDouble();
        }

        throw new Exception("Cannot convert '" + toString() + "'to type '" + type + "'");
    }

    @Override
    public String toString() {
        return value;
    }

    public String toString(String defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isEmpty() {
        return value == null || value.trim().isEmpty();
    }

}
