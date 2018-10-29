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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * When deserializing objects, first check that the class being deserialized is in the allowed whitelist.
 *
 * @author idealzh
 */
public class WhitelistObjectInputStream extends ObjectInputStream {

    private static List<String> whiteClassNames;
    private static List<Pattern> whiteRegExp;

    static {
        loadWhitelist(WhitelistObjectInputStream.class.getResourceAsStream(PippoConstants.LOCATION_OF_PIPPO_WHITELIST_SERIALIZATION));
    }

    public WhitelistObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    protected Class<?> resolveClass(ObjectStreamClass descriptor) throws ClassNotFoundException, IOException {
        String className = descriptor.getName();
        if ((!isWhiteListed(className)) && (!isWhiteListedRegex(className))) {
            throw new InvalidClassException("Unauthorized deserialization attempt", className);
        }

        return super.resolveClass(descriptor);
    }

    private boolean isWhiteListed(String className) {
        for (String name : whiteClassNames) {
            if (name.equals(className)) {
                return true;
            }
        }

        return false;
    }

    private boolean isWhiteListedRegex(String className) {
        for (Pattern pattern : whiteRegExp) {
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
     * A regular expression whitelisting the whole java.lang package and its sub-packages.
     * /java.lang.* /
     *
     * # Pippo
     * ro.pippo.session.DefaultSessionData
     * ro.pippo.core.Flash
     * }
     *
     * A line that starts with {@code #} is a comment and will be ignored.
     * A line that starts and ends with {@code /} is interpreted as a regular expression.
     */
    private static void loadWhitelist(InputStream input) {
        String content;
        try {
            content = IoUtils.toString(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading the whitelist input", e);
        }

        whiteClassNames = new ArrayList<>();

        String[] lines = content.split("[\\r\\n]+");
        for (String line : lines) {
            if (line.startsWith("#")) {
                // it's a comment; ignore line
                continue;
            } else if (line.startsWith("/") && (line.endsWith("/"))) {
                addWhiteRegExp(Pattern.compile(line.substring(1, line.length() - 2)));
            }

            addWhiteClassName(line);
        }
    }

    private static void addWhiteClassName(String className) {
        whiteClassNames.add(className);
    }

    private static void addWhiteRegExp(Pattern pattern) {
        whiteRegExp.add(pattern);
    }

    /**
     * Returns the whitelisted class names.
     * @return the whitelisted class names.
     */
    public static List<String> getWhitelistedClassNames() {
        return whiteClassNames;
    }

    /**
     * Returns the whitelisted regular expressions.
     * @return the whitelisted regular expressions.
     */
    public static List<Pattern> getWhitelistedRegExp() {
        return whiteRegExp;
    }

}
