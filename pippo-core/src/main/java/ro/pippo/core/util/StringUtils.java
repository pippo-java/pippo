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
package ro.pippo.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * String utility functions to keep pippo-core small.
 */
public class StringUtils {

    private StringUtils() {}

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static List<String> getList(String s, String separator) {
        List<String> strings = new ArrayList<>();
        try {
            String[] chunks = s.split(separator + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
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

    /**
     * <p>Removes a substring only if it is at the start of a source string,
     * otherwise returns the source string.</p>
     * <p/>
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     * <p/>
     * <pre>
     * StringUtils.removeStart(null, *)      = null
     * StringUtils.removeStart("", *)        = ""
     * StringUtils.removeStart(*, null)      = *
     * StringUtils.removeStart("www.domain.com", "www.")  = "domain.com"
     * StringUtils.removeStart("abc", "")    = "abc"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     * {@code null} if null String input
     */
    public static String removeStart(String str, String remove) {
        if (isNullOrEmpty(str) || isNullOrEmpty(remove)) {
            return str;
        }

        if (str.startsWith(remove)) {
            return str.substring(remove.length());
        }

        return str;
    }

    /**
     * <p>Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.</p>
     * <p/>
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     * <p/>
     * <pre>
     * StringUtils.removeEnd(null, *)      = null
     * StringUtils.removeEnd("", *)        = ""
     * StringUtils.removeEnd(*, null)      = *
     * StringUtils.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
     * StringUtils.removeEnd("www.domain.com", ".com")   = "www.domain"
     * StringUtils.removeEnd("www.domain.com", "domain") = "www.domain.com"
     * StringUtils.removeEnd("abc", "")    = "abc"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     * {@code null} if null String input
     */
    public static String removeEnd(String str, String remove) {
        if (isNullOrEmpty(str) || isNullOrEmpty(remove)) {
            return str;
        }

        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }

        return str;
    }

    /**
     * <p>Adds a substring only if the source string does not already start with the substring,
     * otherwise returns the source string.</p>
     * <p/>
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     * <p/>
     * <pre>
     * StringUtils.addStart(null, *)      = *
     * StringUtils.addStart("", *)        = *
     * StringUtils.addStart(*, null)      = *
     * StringUtils.addStart("domain.com", "www.")  = "www.domain.com"
     * StringUtils.addStart("abc123", "abc")    = "abc123"
     * </pre>
     *
     * @param str the source String to search, may be null
     * @param add the String to search for and add, may be null
     * @return the substring with the string added if required
     */
    public static String addStart(String str, String add) {
        if (isNullOrEmpty(add)) {
            return str;
        }

        if (isNullOrEmpty(str)) {
            return add;
        }

        if (!str.startsWith(add)) {
            return add + str;
        }

        return str;
    }

    /**
     * <p>Adds a substring only if the source string does not already end with the substring,
     * otherwise returns the source string.</p>
     * <p/>
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     * <p/>
     * <pre>
     * StringUtils.addEnd(null, *)      = *
     * StringUtils.addEnd("", *)        = *
     * StringUtils.addEnd(*, null)      = *
     * StringUtils.addEnd("www.", "domain.com")  = "www.domain.com"
     * StringUtils.addEnd("123abc", "abc")    = "123abc"
     * </pre>
     *
     * @param str the source String to search, may be null
     * @param add the String to search for and add, may be null
     * @return the substring with the string added if required
     */
    public static String addEnd(String str, String add) {
        if (isNullOrEmpty(add)) {
            return str;
        }

        if (isNullOrEmpty(str)) {
            return add;
        }

        if (!str.endsWith(add)) {
            return str + add;
        }

        return str;
    }

    /**
     * Format the string. Replace "{}" with %s and format the string using String.format
     */
    public static String format(String str, Object... args) {
        str = str.replaceAll("\\{}", "%s");

        return String.format(str, args);
    }

    /**
     * Returns the file extension of the value without the dot or an empty string.
     *
     * @param value
     * @return the extension without dot or an empry string
     */
    public static String getFileExtension(String value) {
        int index = value.lastIndexOf('.');
        if (index > -1) {
            return value.substring(index + 1);
        }

        return "";
    }

    /**
     * Returns the prefix of the input string from 0 to the first index of the delimiter OR it returns the input string.
     *
     * @param input
     * @param delimiter
     * @return the prefix substring or the entire input string if the delimiter is not found
     */
    public static String getPrefix(String input, char delimiter) {
        int index = input.indexOf(delimiter);
        if (index > -1) {
            return input.substring(0, index);
        }

        return input;
    }

}
