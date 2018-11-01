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
package ro.pippo.core.util;

import ro.pippo.core.PippoConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * When deserializing objects, first check that the class being deserialized is in the allowed whitelist.
 *
 * @author idealzh
 */
public class WhitelistObjectInputStream extends ObjectInputStream {

    private static Set<String> whiteClassNames;
    private static Set<Pattern> whiteRegEx;

    static {
        loadWhitelist(WhitelistObjectInputStream.class.getResourceAsStream(PippoConstants.LOCATION_OF_PIPPO_WHITELIST_SERIALIZATION));
    }

    public WhitelistObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    protected Class<?> resolveClass(ObjectStreamClass descriptor) throws ClassNotFoundException, IOException {
        String className = descriptor.getName();
        if (!isWhiteClass(className)) {
            throw new InvalidClassException("Unauthorized deserialization attempt", className);
        }

        return super.resolveClass(descriptor);
    }

    private boolean isWhiteClass(String className) {
        // check in list with white classes
        for (String name : whiteClassNames) {
            if (name.equals(className)) {
                return true;
            }
        }

        // check in list with white regex
        for (Pattern pattern : whiteRegEx) {
            if (pattern.matcher(className).matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Load the whitelist from an {@link InputStream}.
     * The content of the {@code InputStream} is in format:
     * {@code
     * # Java
     * java.util.ArrayList
     * java.util.HashMap
     * # A regular expression whitelisting the whole "java.lang" package and its sub-packages
     * >java.lang.*
     *
     * # Pippo
     * ro.pippo.session.DefaultSessionData
     * ro.pippo.core.Flash
     * }
     *
     * A line that starts with {@code #} is a comment and will be ignored.
     * A line that starts with {@code >} is interpreted as a regular expression.
     */
    private static void loadWhitelist(InputStream input) {
        String content;
        try {
            content = IoUtils.toString(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading the whitelist input", e);
        }

        whiteClassNames = new HashSet<>();
        whiteRegEx = new HashSet<>();

        String[] lines = content.split("[\\r\\n]+");
        for (String line : lines) {
            // check for comment
            if (line.startsWith("#")) {
                // it's a comment; ignore line
                continue;
            }

            if (line.startsWith(">")) {
                // it's a regexp
                addWhiteRegEx(line.substring(1).trim());
            } else {
                // it's a regular (full) class name
                addWhiteClassName(line.trim());
            }
        }
    }

    private static void addWhiteClassName(String className) {
        whiteClassNames.add(className);
    }

    private static void addWhiteRegEx(String regex) {
        whiteRegEx.add(Pattern.compile(regex));
    }

    /**
     * Returns the white class names.
     *
     * @return the white class names.
     */
    public static String[] getWhiteClassNames() {
        return whiteClassNames.toArray(new String[0]);
    }

    /**
     * Returns the white regular expressions.
     *
     * @return the white regular expressions.
     */
    public static Pattern[] getWhiteRegEx() {
        return whiteRegEx.toArray(new Pattern[0]);
    }

}
