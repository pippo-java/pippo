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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An utility class that help you to create complex regex paths using only includes and excludes.
 * <p/>
 * <pre>
 * private String securePaths() {
 *     return new PathRegexBuilder()
 *         .excludes(
 *             "/login",
 *             "/admin",
 *             "/webjars",
 *             "/public"
 *         )
 *         .build();
 * }
 *
 * private String secureAdminPaths() {
 *     return new PathRegexBuilder()
 *         .includes(
 *             "/admin"
 *         )
 *         .excludes(
 *             "/admin/login",
 *             "/webjars",
 *             "/public"
 *         )
 *         .build();
 * }
 * </pre>
 *
 * @author Decebal Suiu
 */
public class PathRegexBuilder {

    private List<String> includes;
    private List<String> excludes;

    public PathRegexBuilder() {
        includes = new ArrayList<>();
        excludes = new ArrayList<>();
    }

    public PathRegexBuilder includes(String... includes) {
        this.includes.addAll(Arrays.asList(includes));

        return this;
    }

    public PathRegexBuilder excludes(String... excludes) {
        this.excludes.addAll(Arrays.asList(excludes));

        return this;
    }

    public String build() {
        StringBuilder regex = new StringBuilder();

        // add includes
        StringBuilder regexIncludes = new StringBuilder();
        if (!includes.isEmpty()) {
            regexIncludes.append('^'); // the beginning of a line
            regexIncludes.append('(');
            for (String include : includes) {
                regexIncludes.append(include);
                regexIncludes.append('|'); // or
            }
            regexIncludes.deleteCharAt(regexIncludes.length() - 1);
            regexIncludes.append(").*");
        }

        if (!excludes.isEmpty()) {

        }

        // add excludes
        StringBuilder regexExcludes = new StringBuilder();
        if (!excludes.isEmpty()) {
            regexExcludes.append('^'); // the beginning of a line
            regexExcludes.append("(?!"); // zero-width negative lookahead
            for (String exclude : excludes) {
                regexExcludes.append(exclude);
                regexExcludes.append('|'); // or
            }
            regexExcludes.deleteCharAt(regexExcludes.length() - 1);
            regexExcludes.append(").+");
        }

        if (regexIncludes.length() == 0) {
            regex.append(regexExcludes);
        } else {
            if (regexExcludes.length() == 0) {
                regex.append(regexIncludes);
            } else {
                regex.append("(?=");
                regex.append(regexIncludes);
                regex.append(')');
                regex.append(regexExcludes);
            }
        }

        return regex.toString();
    }

    @Override
    public String toString() {
        return "RegexBuilder{" +
            "includes=" + includes +
            ", excludes=" + excludes +
            '}';
    }

}
