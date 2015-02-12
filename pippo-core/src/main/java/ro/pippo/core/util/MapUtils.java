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
package ro.pippo.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public class MapUtils {

    /**
     * Produces a string similar with a query string <code>key1=value1&key2=value2</code>.
     * @param map
     * @return
     */
    public static String toString(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();

        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            String value = map.get(key);
            try {
                builder.append(URLEncoder.encode(key, "UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return builder.toString();
    }

    public static Map<String, String> fromString(String input) {
        Map<String, String> map = new HashMap<>();

        String[] nameValuePairs = input.split("&");
        for (String nameValuePair : nameValuePairs) {
            String[] nameValue = nameValuePair.split("=");
            try {
                map.put(URLDecoder.decode(nameValue[0], "UTF-8"), URLDecoder.decode(nameValue[1], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return map;
    }

}
