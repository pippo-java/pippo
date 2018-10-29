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

/**
 * When deserializing objects, first check that the class being deserialized is in the allowed whitelist.
 *
 * @author idealzh
 */
public class WhitelistObjectInputStream extends ObjectInputStream {

    private static List<String> whiteClassNames;
    private static List<String> whitePackageNames;

    static {
        loadWhitelist(WhitelistObjectInputStream.class.getResourceAsStream(PippoConstants.LOCATION_OF_PIPPO_WHITELIST_SERIALIZATION));
    }

    public WhitelistObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    protected Class<?> resolveClass(ObjectStreamClass descriptor) throws ClassNotFoundException, IOException {
        String className = descriptor.getName();
        if ((!isWhiteListed(className)) && (!isWhiteListedPackageName(className))) {
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

    private boolean isWhiteListedPackageName(String className) {
        for (String packageName : whitePackageNames) {
            if (className.startsWith(packageName)) {
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
     * # all class names in java.lang (please be aware of the trailing dot (.) aignalling that the whole package and
     * # its sub-packages shall be whitelisted)
     * java.lang.
     *
     * # Pippo
     * ro.pippo.session.DefaultSessionData
     * ro.pippo.core.Flash
     * }
     *
     * A line that starts with {@code #} is a comment and will be ignored.
     * A line that ends with a dot (.) whitelists a complete package and its sub-packages.
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
            } else if (line.endsWith(".")) {
                addWhitePackageName(line);
            }

            addWhiteClassName(line);
        }
    }

    private static void addWhiteClassName(String className) {
        whiteClassNames.add(className);
    }

    private static void addWhitePackageName(String packageName) {
        whitePackageNames.add(packageName);
    }

}
