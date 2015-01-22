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
package ro.fortsoft.pippo.core.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pippo.core.HttpConstants;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.core.controller.ControllerHandler;
import ro.fortsoft.pippo.core.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The routes are matched in the order they are defined.
 *
 * @author Decebal Suiu
 * @author James Moger
 */
public class DefaultRouter implements Router {

    private static final Logger log = LoggerFactory.getLogger(DefaultRouter.class);

    // Matches: {id} AND {id: .*?}
    // group(1) extracts the name of the group (in that case "id").
    // group(3) extracts the regex if defined
    private static final Pattern PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE = Pattern.compile("\\{(.*?)(:\\s(.*?))?\\}");

    // This regex matches everything in between path slashes.
    private static final String VARIABLE_ROUTES_DEFAULT_REGEX = "([^/]*)";

    // This regex works for both {myParam} AND {myParam: .*}
    private static final String VARIABLE_PART_PATTERN_WITH_PLACEHOLDER = "\\{(%s)(:\\s(.*))?\\}";

    // key = request method
    private Map<String, List<PatternBinding>> bindingsCache;

    private List<Route> routes;
    // key = request method
    private Map<String, List<Route>> cache;
    private String contextPath;

    public DefaultRouter() {
        routes = new ArrayList<>();
        cache = new HashMap<>();
        bindingsCache = new HashMap<>();
        contextPath = "";
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public void setContextPath(String contextPath) {
        if (StringUtils.isNullOrEmpty(contextPath) || "/".equals(contextPath.trim())) {
            this.contextPath = "";
        } else {
            this.contextPath = StringUtils.addStart(contextPath, "/");
        }
    }

    /**
     * Prefix the given path with the context path.
     *
     * @param path
     * @return an absolute path
     */
    private String prefixContextPath(String path) {
        return contextPath + StringUtils.addStart(path, "/");
    }

    @Override
    public final List<Route> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    public List<Route> getRoutes(String requestMethod) {
        List<Route> routes = cache.get(requestMethod);
        if (routes != null) {
            routes = Collections.unmodifiableList(routes);
        } else {
            routes = Collections.emptyList();
        }

        return routes;
    }

    protected void validateRoute(Route route) {
        // validate the request method
        if (StringUtils.isNullOrEmpty(route.getRequestMethod())) {
            throw new PippoRuntimeException("Unspecified request method!");
        }

        // validate the uri pattern
        String uriPattern = route.getUriPattern();
        if (StringUtils.isNullOrEmpty(uriPattern)) {
            throw new PippoRuntimeException("The uri pattern cannot be null or empty");
        }
    }

    @Override
    public List<RouteMatch> findRoutes(String requestUri, String requestMethod) {
        log.debug("Finding route matches for {} '{}'", requestMethod, requestUri);

        List<RouteMatch> routeMatches = new ArrayList<>();

        List<PatternBinding> bindings = getBindings(requestMethod);
        for (Route route : routes) { // to preserve the routes order
            for (PatternBinding binding : bindings) {
                if (route.equals(binding.getRoute()) && binding.getPattern().matcher(requestUri).matches()) {
                    // TODO improve (it's possible to have the same uriPattern for many routes => same parameters)
                    routeMatches.add(new RouteMatch(route, getParameters(binding, requestUri)));
                    break;
                }
            }
        }

        return routeMatches;
    }

    @Override
    public void addRoute(Route route) {
        log.debug("Add route for {} '{}'", route.getRequestMethod(), route.getUriPattern());
        validateRoute(route);
        routes.add(route);

        List<Route> cacheEntry = cache.get(route.getRequestMethod());
        if (cacheEntry == null) {
            cacheEntry = new ArrayList<>();
        }
        cacheEntry.add(route);
        cache.put(route.getRequestMethod(), cacheEntry);

        addBinding(route);
    }

    @Override
    public void removeRoute(Route route) {
        log.debug("Removing route for {} '{}'", route.getRequestMethod(), route.getUriPattern());
        routes.remove(route);

        List<Route> cacheEntry = cache.get(route.getRequestMethod());
        if (cacheEntry != null) {
            cacheEntry.remove(route);
        }

        removeBinding(route);
    }

    @Override
    public String uriFor(String uri) {
        return prefixContextPath(uri);
    }

    @Override
    public String uriFor(String uriPattern, Map<String, Object> parameters) {
        PatternBinding binding = getBinding(uriPattern);

        return (binding != null) ? prefixContextPath(uriFor(binding, parameters)) : null;
    }

    @Override
    public String uriFor(Class<? extends Controller> controllerClass, String methodName) {
        Route route = getRoute(controllerClass, methodName);

        return (route != null) ? route.getUriPattern() : null;
    }

    @Override
    public String uriFor(Class<? extends Controller> controllerClass, String methodName, Map<String, Object> parameters) {
        Route route = getRoute(controllerClass, methodName);

        return (route != null) ? prefixContextPath(uriFor(route.getUriPattern(), parameters)) : null;
    }

    private Route getRoute(Class<? extends Controller> controllerClass, String methodName) {
        List<Route> routes = getRoutes();
        for (Route route : routes) {
            RouteHandler routeHandler = route.getRouteHandler();
            if (routeHandler instanceof ControllerHandler) {
                ControllerHandler controllerHandler = (ControllerHandler) routeHandler;
                if (controllerClass == controllerHandler.getControllerClass()
                        && methodName.equals(controllerHandler.getMethodName())) {
                    return route;
                }
            }
        }

        return null;
    }

    @Override
    public String uriPatternFor(Class<? extends ClasspathResourceHandler> resourceHandlerClass) {
        Route route = getRoute(resourceHandlerClass);

        return (route != null) ? route.getUriPattern() : null;
    }

    private Route getRoute(Class<? extends ClasspathResourceHandler> resourceHandlerClass) {
        List<Route> routes = getRoutes();
        for (Route route : routes) {
            RouteHandler routeHandler = route.getRouteHandler();
            if (resourceHandlerClass.isAssignableFrom(routeHandler.getClass())) {
                ClasspathResourceHandler resourceHandler = (ClasspathResourceHandler) routeHandler;
                if (resourceHandlerClass == resourceHandler.getClass()) {
                    return route;
                }
            }
        }

        return null;
    }

    private void addBinding(Route route) {
        String uriPattern = route.getUriPattern();
        // TODO improve (it's possible to have the same uriPattern for many routes => same pattern)
        String regex = getRegex(uriPattern);
        Pattern pattern = Pattern.compile(regex);
        List<String> parameterNames = getParameterNames(uriPattern);
        PatternBinding binding = new PatternBinding(pattern, route, parameterNames);
        String requestMethod = route.getRequestMethod();
        if (!bindingsCache.containsKey(requestMethod)) {
            bindingsCache.put(requestMethod, new ArrayList<PatternBinding>());
        }
        bindingsCache.get(requestMethod).add(binding);
    }

    private void removeBinding(Route route) {
        PatternBinding binding = getBinding(route.getUriPattern());
        bindingsCache.get(route.getRequestMethod()).remove(binding);
    }

    private PatternBinding getBinding(String uriPattern) {
        Collection<List<PatternBinding>> values = bindingsCache.values();
        for (List<PatternBinding> bindings : values) {
            for (PatternBinding binding : bindings) {
                if (uriPattern.equals(binding.getRoute().getUriPattern())) {
                    return binding;
                }
            }
        }

        return null;
    }

    private List<PatternBinding> getBindings(String requestMethod) {
        List<PatternBinding> bindings = new ArrayList<>();

        if (bindingsCache.containsKey(requestMethod)) {
            bindings.addAll(bindingsCache.get(requestMethod));
        }

        if (bindingsCache.containsKey(HttpConstants.Method.ALL)) {
            bindings.addAll(bindingsCache.get(HttpConstants.Method.ALL));
        }

        return bindings;
    }

    /**
     * Transforms an url pattern like "/{name}/id/*" into a regex like "/([^/]*)/id/*."
     *
     * Also handles regular expressions if defined inside routes:
     * For instance "/users/{username: [a-zA-Z][a-zA-Z_0-9]}" becomes
     * "/users/([a-zA-Z][a-zA-Z_0-9])"
     *
     * @return The converted regex with default matching regex - or the regex
     *          specified by the user.
     */
    private String getRegex(String urlPattern) {
        Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(urlPattern);

        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            // By convention group 3 is the regex if provided by the user.
            // If it is not provided by the user the group 3 is null.
            String namedVariablePartOfRoute = matcher.group(3);
            String namedVariablePartOfORouteReplacedWithRegex;

            if (namedVariablePartOfRoute != null) {
                // we convert that into a regex matcher group itself
                namedVariablePartOfORouteReplacedWithRegex = "(" + Matcher.quoteReplacement(namedVariablePartOfRoute) + ")";
            } else {
                // we convert that into the default namedVariablePartOfRoute regex group
                namedVariablePartOfORouteReplacedWithRegex = VARIABLE_ROUTES_DEFAULT_REGEX;
            }
            // we replace the current namedVariablePartOfRoute group
            matcher.appendReplacement(stringBuffer, namedVariablePartOfORouteReplacedWithRegex);
        }

        // .. and we append the tail to complete the stringBuffer
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    /**
     * Extracts the name of the parameters from a route
     *
     * /{my_id}/{my_name}
     *
     * would return a List with "my_id" and "my_name"
     *
     * @param uriPattern
     * @return a list with the names of all parameters in the url pattern
     */
    private List<String> getParameterNames(String uriPattern) {
        List<String> list = new ArrayList<>();

        Matcher m = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(uriPattern);

        while (m.find()) {
            // group(1) is the name of the group. Must be always there...
            // "/assets/{file}" and "/assets/{file: [a-zA-Z][a-zA-Z_0-9]}"
            // will return file.
            list.add(m.group(1));
        }

        return list;
    }

    private Map<String, String> getParameters(PatternBinding binding, String requestUri) {
        Map<String, String> parameters = new HashMap<>();

        List<String> parameterNames = binding.getParameterNames();
        Matcher matcher = binding.getPattern().matcher(requestUri);
        matcher.matches();
        int groupCount = matcher.groupCount();
        if (groupCount > 0) {
            for (int i = 1; i <= groupCount; i++) {
                parameters.put(parameterNames.get(i - 1), matcher.group(i));
            }
        }

        return parameters;
    }

    private String uriFor(PatternBinding binding, Map<String, Object> parameters) {
        String uri = binding.getRoute().getUriPattern();

        List<String> parameterNames = binding.getParameterNames();
        if (!parameters.keySet().containsAll(parameterNames)) {
            log.error("You must provide values for all path parameters. {} vs {}", parameterNames, parameters.keySet());
        }

        Map<String, Object> queryParameters = new HashMap<>(parameters.size());

        for (Entry<String, Object> parameterPair : parameters.entrySet()) {
            boolean foundAsPathParameter = false;

            StringBuffer sb = new StringBuffer();
            String buffer = String.format(VARIABLE_PART_PATTERN_WITH_PLACEHOLDER, parameterPair.getKey());

            Pattern pattern = Pattern.compile(buffer);
            Matcher matcher = pattern.matcher(uri);
            while (matcher.find()) {
                String pathValue = parameterPair.getValue().toString();
                matcher.appendReplacement(sb, pathValue);
                foundAsPathParameter = true;
            }

            matcher.appendTail(sb);
            uri = sb.toString();

            if (!foundAsPathParameter) {
                queryParameters.put(parameterPair.getKey(), parameterPair.getValue());
            }
        }

        // now prepare the query string for this url if we got some query params
        if (!queryParameters.isEmpty()) {
            // add remaining parameters as query parameters
            StringBuilder query = new StringBuilder();
            Iterator<Entry<String, Object>> iterator = queryParameters.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Object> parameterEntry = iterator.next();
                String parameterName = parameterEntry.getKey();
                Object parameterValue =  parameterEntry.getValue();
                // TODO consider using URLEncoder on the parameter value
                query.append(parameterName).append("=").append(parameterValue.toString());

                if (iterator.hasNext()) {
                    query.append("&");
                }
            }

            uri += "?" + query;
        }

        return uri;
    }

    private class PatternBinding {

        private final Pattern pattern;
        private final Route route;
        private final List<String> parameterNames;

        private PatternBinding(Pattern pattern, Route route, List<String> parameterNames) {
            this.pattern = pattern;
            this.route = route;
            this.parameterNames = parameterNames;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public Route getRoute() {
            return route;
        }

        public List<String> getParameterNames() {
            return parameterNames;
        }

        @Override
        public String toString() {
            return "PatternBinding{" +
                    "pattern=" + pattern +
                    ", route=" + route +
                    ", parameterNames=" + parameterNames +
                    '}';
        }

    }

}
