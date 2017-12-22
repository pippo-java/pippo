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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Decebal Suiu
 */
public class DefaultUriMatcher implements UriMatcher {

    private static final Logger log = LoggerFactory.getLogger(DefaultUriMatcher.class);

    // Matches: {id} AND {id: .*?}
    // group(1) extracts the name of the group (in that case "id").
    // group(3) extracts the regex if defined
    private static final Pattern PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE = Pattern.compile("\\{(.*?)(:\\s(.*?))?\\}");

    // This regex matches everything in between path slashes.
    private static final String VARIABLE_ROUTES_DEFAULT_REGEX = "(?<%s>[^/]+)";

    // This regex works for both {myParam} AND {myParam: .*}
    private static final String VARIABLE_PART_PATTERN_WITH_PLACEHOLDER = "\\{(%s)(:\\s([^}]*))?\\}";

    private static final String PATH_PARAMETER_REGEX_GROUP_NAME_PREFIX = "param";

    // key = uri pattern
    private Map<String, UriPatternBinding> bindings;

    public DefaultUriMatcher() {
        bindings = new HashMap<>();
    }

    @Override
    public Map<String, String> match(String requestUri, String uriPattern) {
        UriPatternBinding binding = bindings.get(uriPattern);
        if (binding == null) {
            // something is wrong
            throw new PippoRuntimeException("No binding for '{}'. Create binding with 'addUriPattern'.", uriPattern);
        }

        return binding.getPattern().matcher(requestUri).matches() ? getParameters(binding, requestUri) : null;
    }

    @Override
    public UriPatternBinding addUriPattern(String uriPattern) {
        if (bindings.containsKey(uriPattern)) {
            return bindings.get(uriPattern);
        }

        String regex = getRegex(uriPattern);
        Pattern pattern = Pattern.compile(regex);
        List<String> parameterNames = getParameterNames(uriPattern);
        UriPatternBinding binding = new UriPatternBinding(uriPattern, pattern, parameterNames);
        bindings.put(uriPattern, binding);
        log.trace("Add binding '{}'", binding);

        return binding;
    }

    @Override
    public UriPatternBinding removeUriPattern(String uriPattern) {
        return bindings.remove(uriPattern);
    }

    @Override
    public String uriFor(String uriPattern, Map<String, Object> parameters) {
        UriPatternBinding binding = bindings.get(uriPattern);
        if (binding == null) {
            // something is wrong
            throw new PippoRuntimeException("No binding for '{}'. Create binding with 'addUriPattern'.", uriPattern);
        }

        List<String> parameterNames = binding.getParameterNames();
        if (!parameters.keySet().containsAll(parameterNames)) {
            log.error("You must provide values for all path parameters. {} vs {}", parameterNames, parameters.keySet());
        }

        Map<String, Object> queryParameters = new HashMap<>(parameters.size());

        // create a uri starting from uriPattern (that can contains path params placeholders)
        String uri = binding.getUriPattern();

        // replace path params placeholders from uri pattern
        for (Map.Entry<String, Object> parameterPair : parameters.entrySet()) {
            boolean foundAsPathParameter = false;

            StringBuffer sb = new StringBuffer();
            String regex = String.format(VARIABLE_PART_PATTERN_WITH_PLACEHOLDER, parameterPair.getKey());
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(uri);
            while (matcher.find()) {
                matcher.appendReplacement(sb, getPathParameterValue(uriPattern, parameterPair.getKey(), parameterPair.getValue()));
                foundAsPathParameter = true;
            }

            matcher.appendTail(sb);
            uri = sb.toString();

            if (!foundAsPathParameter) {
                queryParameters.put(parameterPair.getKey(), parameterPair.getValue());
            }
        }

        // prepare the query string for this url if we got some query params
        if (!queryParameters.isEmpty()) {
            // add remaining parameters as query parameters
            StringBuilder query = new StringBuilder();
            Iterator<Map.Entry<String, Object>> iterator = queryParameters.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> parameterEntry = iterator.next();
                String parameterName = parameterEntry.getKey();
                Object parameterValue = parameterEntry.getValue();
                String encodedParameterValue;
                try {
                    encodedParameterValue = URLEncoder.encode(parameterValue.toString(), PippoConstants.UTF8);
                } catch (UnsupportedEncodingException e) {
                    throw new PippoRuntimeException(e, "Cannot encode the parameter value '{}'", parameterValue.toString());
                }
                query.append(parameterName).append("=").append(encodedParameterValue);

                if (iterator.hasNext()) {
                    query.append("&");
                }
            }

            uri += "?" + query;
        }

        return uri;
    }

    protected String getPathParameterValue(String uriPattern, String parameterName, Object parameterValue) {
        return parameterValue.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getParameters(UriPatternBinding binding, String requestUri) {
        List<String> parameterNames = binding.getParameterNames();
        if (parameterNames.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        Map<String, String> parameters = new HashMap<>();
        Matcher matcher = binding.getPattern().matcher(requestUri);
        matcher.matches(); // always true
        int groupCount = matcher.groupCount();
        if (groupCount > 0) {
            for (int i = 0; i < parameterNames.size(); i++) {
                parameters.put(parameterNames.get(i), matcher.group(getPathParameterRegexGroupName(i)));
            }
        }

        return parameters;
    }

    /**
     * Transforms an url pattern like "/{name}/id/*" into a regex like "/([^/]*)/id/*."
     * <p/>
     * Also handles regular expressions if defined inside routes:
     * For instance "/users/{username: [a-zA-Z][a-zA-Z_0-9]}" becomes
     * "/users/([a-zA-Z][a-zA-Z_0-9])"
     *
     * @return The converted regex with default matching regex - or the regex
     * specified by the user.
     */
    private String getRegex(String urlPattern) {
        StringBuffer buffer = new StringBuffer();

        Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(urlPattern);
        int pathParameterIndex = 0;
        while (matcher.find()) {
            // By convention group 3 is the regex if provided by the user.
            // If it is not provided by the user the group 3 is null.
            String namedVariablePartOfRoute = matcher.group(3);
            String namedVariablePartOfORouteReplacedWithRegex;

            if (namedVariablePartOfRoute != null) {
                // we convert that into a regex matcher group itself
                String variableRegex = replacePosixClasses(namedVariablePartOfRoute);
                namedVariablePartOfORouteReplacedWithRegex = String.format("(?<%s>%s)",
                    getPathParameterRegexGroupName(pathParameterIndex), Matcher.quoteReplacement(variableRegex));
            } else {
                // we convert that into the default namedVariablePartOfRoute regex group
                namedVariablePartOfORouteReplacedWithRegex = String.format(VARIABLE_ROUTES_DEFAULT_REGEX,
                    getPathParameterRegexGroupName(pathParameterIndex));
            }
            // we replace the current namedVariablePartOfRoute group
            matcher.appendReplacement(buffer, namedVariablePartOfORouteReplacedWithRegex);
            pathParameterIndex++;
        }

        // .. and we append the tail to complete the stringBuffer
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private String getPathParameterRegexGroupName(int pathParameterIndex) {
        return PATH_PARAMETER_REGEX_GROUP_NAME_PREFIX + pathParameterIndex;
    }

    /**
     * Replace any specified POSIX character classes with the Java equivalent.
     *
     * @param input
     * @return a Java regex
     */
    private String replacePosixClasses(String input) {
        return input
            .replace(":alnum:", "\\p{Alnum}")
            .replace(":alpha:", "\\p{L}")
            .replace(":ascii:", "\\p{ASCII}")
            .replace(":digit:", "\\p{Digit}")
            .replace(":xdigit:", "\\p{XDigit}");
    }

    /**
     * Extracts the name of the parameters from a route
     * <p/>
     * /{my_id}/{my_name}
     * <p/>
     * would return a List with "my_id" and "my_name"
     *
     * @param uriPattern
     * @return a list with the names of all parameters in the url pattern
     */
    private List<String> getParameterNames(String uriPattern) {
        List<String> list = new ArrayList<>();

        Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(uriPattern);
        while (matcher.find()) {
            // group(1) is the name of the group. Must be always there...
            // "/assets/{file}" and "/assets/{file: [a-zA-Z][a-zA-Z_0-9]}"
            // will return file.
            list.add(matcher.group(1));
        }

        return list;
    }

}
