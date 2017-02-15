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
package ro.pippo.core.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.Stack;
import ro.pippo.core.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    private static final String VARIABLE_ROUTES_DEFAULT_REGEX = "(?<%s>[^/]+)";

    // This regex works for both {myParam} AND {myParam: .*}
    private static final String VARIABLE_PART_PATTERN_WITH_PLACEHOLDER = "\\{(%s)(:\\s([^}]*))?\\}";

    private static final String PATH_PARAMETER_REGEX_GROUP_NAME_PREFIX = "param";

    // key = requestMethod
    private Map<String, List<Route>> routesCache;

    // key = requestMethod
    private Map<String, List<Route>> compiledRoutesCache;

    private List<Route> routes;
    private List<Route> compiledRoutes;
    private List<RouteTransformer> transformers;
    private Set<String> ignorePaths;
    private String contextPath;
    private String applicationPath;

    public DefaultRouter() {
        routes = new LinkedList<>();
        compiledRoutes = new LinkedList<>();
        transformers = new ArrayList<>();
        ignorePaths = new TreeSet<>();
        routesCache = new HashMap<>();
        compiledRoutesCache = new HashMap<>();
        contextPath = "";
        applicationPath = "";
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
     * Prefix the given path with the application path.
     *
     * @param path
     * @return an absolute path
     */
    protected String prefixApplicationPath(String path) {
        return applicationPath + StringUtils.addStart(path, "/");
    }

    @Override
    public Set<String> getIgnorePaths() {
        return ignorePaths;
    }

    @Override
    public void ignorePaths(String... pathPrefixes) {
        for (String pathPrefix : pathPrefixes) {
            this.ignorePaths.add(StringUtils.addStart(pathPrefix, "/"));
        }
    }

    @Override
    public final List<Route> getRoutes() {
        List<Route> allRoutes = new ArrayList<>();
        allRoutes.addAll(routes);
        allRoutes.addAll(compiledRoutes);

        return Collections.unmodifiableList(allRoutes);
    }

    @Override
    public void compileRoutes() {
        if (routes.isEmpty()) {
            // nothing to do
            return;
        }

        log.debug("Compile routes");
        Iterator<Route> it = routes.iterator();
        while (it.hasNext()) {
            Route route = it.next();

            // remove route
            it.remove();

            // updates routes' cache
            List<Route> cacheEntry = routesCache.get(route.getRequestMethod());
            if (cacheEntry != null) {
                cacheEntry.remove(route);
            }

            // compile route and apply the transformers
            Route compiledRoute = compileRoute(route);
            for (RouteTransformer transformer : transformers) {
                compiledRoute = transformer.transform(compiledRoute);
                if (compiledRoute == null) {
                    break;
                }
                compiledRoute.bind("__transformer", transformer);
            }

            if (compiledRoute != null) {
                // add the compiled route to list
                addCompiledRoute(compiledRoute);
            }
        }
    }

    public List<Route> getRoutes(String requestMethod) {
        List<Route> allRoutes = new ArrayList<>();

        // add routes that are not compiled yet
        if (routesCache.containsKey(requestMethod)) {
            allRoutes.addAll(routesCache.get(requestMethod));
        }

        // add compiled routes
        allRoutes.addAll(getCompiledRoutes(requestMethod));

        return Collections.unmodifiableList(allRoutes);
    }

    @Override
    public List<RouteMatch> findRoutes(String requestMethod, String requestUri) {
        log.trace("Finding route matches for {} '{}'", requestMethod, requestUri);

        // force compile routes
        compileRoutes();

        List<RouteMatch> routeMatches = compiledRoutes.stream()
            .filter(route -> {
                boolean matches = route.getRequestMethod().equals(requestMethod)
                    || route.getRequestMethod().equals(HttpConstants.Method.ALL);

                if (matches) {
                    Pattern pattern = route.getAttribute("__pattern");
                    matches = pattern.matcher(requestUri).matches();
                }

                return matches;
            })
            .map(route -> new RouteMatch(route, getParameters(route, requestUri)))
            .collect(Collectors.toList());

        log.debug("Found {} route matches for {} '{}'", routeMatches.size(), requestMethod, requestUri);

        return routeMatches;
    }

    @Override
    public void addRoute(Route route) {
        log.debug("Add route for {} '{}'", route.getRequestMethod(), route.getUriPattern());
        validateRoute(route);
        routes.add(route);

        // update cache
        List<Route> cacheEntry = routesCache.get(route.getRequestMethod());
        if (cacheEntry == null) {
            cacheEntry = new ArrayList<>();
        }
        cacheEntry.add(route);
        routesCache.put(route.getRequestMethod(), cacheEntry);
    }

    @Override
    public void removeRoute(Route route) {
        log.debug("Removing route for {} '{}'", route.getRequestMethod(), route.getUriPattern());
        boolean removed = routes.remove(route);
        if (removed) {
            // updates cache
            List<Route> cacheEntry = routesCache.get(route.getRequestMethod());
            if (cacheEntry != null) {
                cacheEntry.remove(route);
            }

            removeCompiledRoute(route);
        }
    }

    @Override
    public void addRouteGroup(RouteGroup routeGroup) {
        // add routes of group
        routeGroup.getRoutes().forEach(route -> {
            String uriPattern = routeGroup.getUriPattern();

            // stack with names
            Stack<String> nameStack =  new Stack<>();
            nameStack.push(route.getName());
            nameStack.pushIfNotEmpty(routeGroup.getName());

            // stack with attributes
            Stack<Map<String, Object>> attributesStack =  new Stack<>();
            attributesStack.push(route.getAttributes());
            attributesStack.push(routeGroup.getAttributes());

            RouteGroup parent = routeGroup.getParent();
            while (parent != null) {
                uriPattern = concatUriPattern(parent.getUriPattern(), uriPattern);

                // push group name to stack
                nameStack.pushIfNotEmpty(parent.getName());

                // push group attributes to stack
                attributesStack.push(parent.getAttributes());

                parent = parent.getParent();
            }
            route.setAbsoluteUriPattern(concatUriPattern(uriPattern, route.getUriPattern()));

            // set route name
            if (!nameStack.isEmpty()) {
                route.setName(StreamSupport.stream(nameStack.spliterator(), false)
                    .collect(Collectors.joining("")));
            }

            // set route attributes
            if (!attributesStack.isEmpty()) {
                route.bindAll(StreamSupport.stream(attributesStack.spliterator(), false)
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
            }

            addRoute(route);
        });

        // add children of group
        routeGroup.getChildren().forEach(this::addRouteGroup);
    }

    @Override
    public void removeRouteGroup(RouteGroup routeGroup) {
        // remove routes of group
        routeGroup.getRoutes().forEach(this::removeRoute);

        // remove children of group
        routeGroup.getChildren().forEach(this::removeRouteGroup);
    }

    @Override
    public String uriFor(String relativeUri) {
        return prefixApplicationPath(relativeUri);
    }

    @Override
    public String uriFor(String nameOrUriPattern, Map<String, Object> parameters) {
        // force compile routes
        compileRoutes();

        Route compiledRoute = getCompiledRoute(nameOrUriPattern);

        return (compiledRoute != null) ? prefixApplicationPath(uriFor(compiledRoute, parameters)) : null;
    }

    @Override
    public String uriPatternFor(Class<? extends ResourceHandler> resourceHandlerClass) {
        Route route = getRoute(resourceHandlerClass);

        return (route != null) ? route.getUriPattern() : null;
    }

    @Override
    public String getApplicationPath() {
        return applicationPath;
    }

    @Override
    public void setApplicationPath(String applicationPath) {
        if (StringUtils.isNullOrEmpty(applicationPath) || "/".equals(applicationPath.trim())) {
            this.applicationPath = "";
        } else {
            this.applicationPath = StringUtils.removeEnd(StringUtils.addStart(applicationPath, "/"), "/");
        }
    }

    @Override
    public void addRouteTransformer(RouteTransformer transformer) {
        log.debug("Add transformer '{}'", transformer.getClass().getSimpleName());
        transformers.add(transformer);
    }

    @Override
    public List<RouteTransformer> getRouteTransformers() {
        return transformers;
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

    private Route getRoute(Class<? extends ResourceHandler> resourceHandlerClass) {
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

    private Route compileRoute(Route route) {
        String uriPattern = route.getUriPattern();
        // TODO improve (it's possible to have the same uriPattern for many routes => same pattern)
        String regex = getRegex(uriPattern);
        List<String> parameterNames = getParameterNames(uriPattern);

        Route compiledRoute = new Route(route);
        // add additional metadata as attributes
        compiledRoute.bind("__regex", regex);
        compiledRoute.bind("__pattern", Pattern.compile(regex));
        compiledRoute.bind("__parameterNames", parameterNames);

        return compiledRoute;
    }

    private void addCompiledRoute(Route compiledRoute) {
        compiledRoutes.add(compiledRoute);

        // update cache
        String requestMethod = compiledRoute.getRequestMethod();
        if (!compiledRoutesCache.containsKey(requestMethod)) {
            compiledRoutesCache.put(requestMethod, new ArrayList<>());
        }
        compiledRoutesCache.get(requestMethod).add(compiledRoute);
    }

    private void removeCompiledRoute(Route route) {
        String nameOrUriPattern = StringUtils.isNullOrEmpty(route.getName()) ? route.getUriPattern() : route.getName();
        Route compiledRoute = getCompiledRoute(nameOrUriPattern);
        compiledRoutes.remove(compiledRoute);

        // update cache
        List<Route> cacheEntry = compiledRoutesCache.get(route.getRequestMethod());
        if (cacheEntry != null) {
            cacheEntry.remove(compiledRoute);
        }
    }

    private Route getCompiledRoute(String nameOrUriPattern) {
        for (Route route : compiledRoutes) {
            if (nameOrUriPattern.equals(route.getName()) || nameOrUriPattern.equals(route.getUriPattern())) {
                return route;
            }
        }

        return null;
    }

    private List<Route> getCompiledRoutes(String requestMethod) {
        List<Route> compiledRoutes = new ArrayList<>();

        if (compiledRoutesCache.containsKey(requestMethod)) {
            compiledRoutes.addAll(compiledRoutesCache.get(requestMethod));
        }

        return compiledRoutes;
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

    private static String getPathParameterRegexGroupName(int pathParameterIndex) {
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

    @SuppressWarnings("unchecked")
    private Map<String, String> getParameters(Route compiledRoute, String requestUri) {
        List<String> parameterNames = compiledRoute.getAttribute("__parameterNames");
        if (parameterNames.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        Map<String, String> parameters = new HashMap<>();
        Pattern pattern = compiledRoute.getAttribute("__pattern");
        Matcher matcher = pattern.matcher(requestUri);
        matcher.matches();
        int groupCount = matcher.groupCount();
        if (groupCount > 0) {
            for (int i = 0; i < parameterNames.size(); i++) {
                parameters.put(parameterNames.get(i), matcher.group(getPathParameterRegexGroupName(i)));
            }
        }

        return parameters;
    }

    private String uriFor(Route compiledRoute, Map<String, Object> parameters) {
        RouteHandler handler = compiledRoute.getRouteHandler();
        boolean isResourceRoute = ResourceHandler.class.isAssignableFrom(handler.getClass());

        List<String> parameterNames = compiledRoute.getAttribute("__parameterNames");
        if (!parameters.keySet().containsAll(parameterNames)) {
            log.error("You must provide values for all path parameters. {} vs {}", parameterNames, parameters.keySet());
        }

        Map<String, Object> queryParameters = new HashMap<>(parameters.size());

        String uri = compiledRoute.getUriPattern();

        for (Entry<String, Object> parameterPair : parameters.entrySet()) {
            boolean foundAsPathParameter = false;

            StringBuffer sb = new StringBuffer();
            String regex = String.format(VARIABLE_PART_PATTERN_WITH_PLACEHOLDER, parameterPair.getKey());
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(uri);
            while (matcher.find()) {
                String pathValue = parameterPair.getValue().toString();
                if (isResourceRoute && ResourceHandler.PATH_PARAMETER.equals(parameterPair.getKey())) {
                    ResourceHandler resourceHandler = (ResourceHandler) compiledRoute.getRouteHandler();
                    if (resourceHandler.isVersioned()) {
                        pathValue = resourceHandler.injectVersion(pathValue);
                    }
                }
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

    private String concatUriPattern(String prefix, String uriPattern) {
        uriPattern = StringUtils.addStart(StringUtils.addStart(uriPattern, "/"), prefix);

        return "/".equals(uriPattern) ? uriPattern : StringUtils.removeEnd(uriPattern, "/");
    }

    /*
    private boolean isCompiledRoute(Route route) {
        return route.getAttributes().containsKey("__regex");
    }
    */

}
