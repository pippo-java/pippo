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
package ro.fortsoft.pippo.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * String utility functions to keep pippo-core small.
 */
public class StringUtils {

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static List<String> getStringList(String value, String separator) {
        List<String> strings = new ArrayList<String>();
        try {
            String[] chunks = value.split(separator + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String chunk : chunks) {
                chunk = chunk.trim();
                if (chunk.length() > 0) {
                    if (chunk.charAt(0) == '"' && chunk.charAt(chunk.length() - 1) == '"') {
                        // strip double quotes
                        chunk = chunk.substring(1, chunk.length() - 1).trim();
                    }
                    strings.add(chunk);
                }
            }
        } catch (PatternSyntaxException e) {
            throw new RuntimeException(e);
        }

        return Collections.unmodifiableList(strings);
    }

}
