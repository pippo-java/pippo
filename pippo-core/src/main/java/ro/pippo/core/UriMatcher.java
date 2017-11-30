/*
 * Copyright (C) 2017 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * {@code UriMatcher} provides the functionality of comparing a given {@code requestUri}
 * with a {@code uriPattern}.
 *
 * @author Decebal Suiu
 */
public interface UriMatcher {

    /**
     * Returns {@code null} for no matching and {@code not null} for matching.
     * The returned map represent the path parameters (the key is the parameter name
     * and the value is parameter value). The returned map is empty for matching but
     * without parameters.
     *
     * @param requestUri
     * @param uriPattern
     * @return
     */
    Map<String, String> match(String requestUri, String uriPattern);

    UriPatternBinding addUriPattern(String uriPattern);

    UriPatternBinding removeUriPattern(String uriPattern);

    String uriFor(String uriPattern, Map<String, Object> parameters);

    class UriPatternBinding {

        private final String uriPattern;
        private final Pattern pattern;
        private final List<String> parameterNames;

        public UriPatternBinding(String uriPattern, Pattern pattern, List<String> parameterNames) {
            this.uriPattern = uriPattern;
            this.pattern = pattern;
            this.parameterNames = parameterNames;
        }

        public String getUriPattern() {
            return uriPattern;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public List<String> getParameterNames() {
            return parameterNames;
        }

        @Override
        public String toString() {
            return "UriPatternBinding{" +
                "uriPattern=" + uriPattern +
                ", pattern=" + pattern +
                ", parameterNames=" + parameterNames +
                '}';
        }

    }

}
