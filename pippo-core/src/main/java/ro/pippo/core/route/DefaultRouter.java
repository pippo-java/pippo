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
import ro.pippo.core.DefaultUriMatcher;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.UriMatcher;
import ro.pippo.core.util.Stack;
import ro.pippo.core.util.StringUtils;

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

    private List<Route> routes;
    private List<Route> compiledRoutes;
    private List<RouteTransformer> transformers;
    private Set<String> ignorePaths;
    private String contextPath;
    private String applicationPath;

    // key = requestMethod
    private Map<String, List<Route>> routesCache;

    // key = requestMethod
    private Map<String, List<Route>> compiledRoutesCache;

    private UriMatcher uriMatcher;

    public DefaultRouter() {
        routes = new LinkedList<>();
        compiledRoutes = new LinkedList<>();
        transformers = new ArrayList<>();
        ignorePaths = new TreeSet<>();
        routesCache = new HashMap<>();
        compiledRoutesCache = new HashMap<>();
        contextPath = "";
        applicationPath = "";

        uriMatcher = new DefaultUriMatcher() {

            @Override
            protected String getPathParameterValue(String uriPattern, String parameterName, Object parameterValue) {
                String value = super.getPathParameterValue(uriPattern, parameterName, parameterValue);

                // retrieve the compiled route for uriPattern
                Route compiledRoute = compiledRoutes.stream()
                    .filter(route -> uriPattern.equals(route.getUriPattern()))
                    .findAny()
                    .orElseThrow(() -> new PippoRuntimeException("Cannot find a compiled route for '{}'", uriPattern));

                RouteHandler handler = compiledRoute.getRouteHandler();
                boolean isResourceRoute = ResourceHandler.class.isAssignableFrom(handler.getClass());

                // inject version for resource routes
                if (isResourceRoute && ResourceHandler.PATH_PARAMETER.equals(parameterName)) {
                    ResourceHandler resourceHandler = (ResourceHandler) compiledRoute.getRouteHandler();
                    if (resourceHandler.isVersioned()) {
                        value = resourceHandler.injectVersion(value);
                    }
                }

                return value;
            }

        };
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

        List<RouteMatch> routeMatches = new ArrayList<>();
        for (Route route : compiledRoutes) {
            boolean methodMatches = route.getRequestMethod().equals(requestMethod)
                || route.getRequestMethod().equals(HttpConstants.Method.ANY);

            if (methodMatches) {
                String uriPattern = route.getUriPattern();
                Map<String, String> parameters = uriMatcher.match(requestUri, uriPattern);
                if (parameters != null) {
                    // complete matching => add route match ro returned list
                    routeMatches.add(new RouteMatch(route, parameters));
                }
            }
        }

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
        if (compiledRoute == null) {
            return null;
        }

        return prefixApplicationPath(uriMatcher.uriFor(compiledRoute.getUriPattern(), parameters));

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
        UriMatcher.UriPatternBinding binding = uriMatcher.addUriPattern(uriPattern);
        List<String> parameterNames = binding.getParameterNames();

        Route compiledRoute = new Route(route);
        // add additional metadata as attributes
        compiledRoute.bind("__pattern", binding.getPattern());
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

        // it's added on compileRoute method
//        uriMatcher.addUriPattern(compiledRoute.getUriPattern());
    }

    private void removeCompiledRoute(Route route) {
        String nameOrUriPattern = StringUtils.isNullOrEmpty(route.getName()) ? route.getUriPattern() : route.getName();
        Route compiledRoute = getCompiledRoute(nameOrUriPattern);
        if (compiledRoute == null) {
            // do nothing; probably the route was not yet compiled
            return;
        }

        compiledRoutes.remove(compiledRoute);

        // update cache
        List<Route> cacheEntry = compiledRoutesCache.get(route.getRequestMethod());
        if (cacheEntry != null) {
            cacheEntry.remove(compiledRoute);
        }

        uriMatcher.removeUriPattern(compiledRoute.getUriPattern());
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

    private String concatUriPattern(String prefix, String uriPattern) {
        uriPattern = StringUtils.addStart(StringUtils.addStart(uriPattern, "/"), prefix);

        return "/".equals(uriPattern) ? uriPattern : StringUtils.removeEnd(uriPattern, "/");
    }

    /*
    private boolean isCompiledRoute(Route route) {
        return route.getAttributes().containsKey("__pattern");
    }
    */

}
