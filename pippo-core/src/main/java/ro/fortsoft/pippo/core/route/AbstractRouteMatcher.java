/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.core.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pippo.core.HttpConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public abstract class AbstractRouteMatcher implements RouteMatcher {

    private static final Logger log = LoggerFactory.getLogger(AbstractRouteMatcher.class);

    private List<Route> routes;
    private Map<String, List<Route>> cache;

    public AbstractRouteMatcher() {
        routes = new ArrayList<Route>();
        cache = new HashMap<String, List<Route>>();
    }

    @Override
    public void addRoute(Route route) throws Exception {
        log.debug("Add route for '{} {}'", route.getRequestMethod(), route.getUrlPattern());
        validateRoute(route);
        routes.add(route);

        List cacheEntry = cache.get(route.getRequestMethod());
        if (cacheEntry == null) {
            cacheEntry = new ArrayList();
        }
        cacheEntry.add(route);
        cache.put(route.getRequestMethod(), cacheEntry);
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

    protected void validateRoute(Route route) throws Exception {
        // validate the request method
        if (!existsRequestMethod(route.getRequestMethod())) {
            throw new Exception("Invalid request method: " + route.getRequestMethod());
        }

        // validate the url pattern
        String urlPattern = route.getUrlPattern();
        if (urlPattern == null || urlPattern.isEmpty()) {
            throw new Exception("The url pattern cannot be null or empty");
        }
    }

    private boolean existsRequestMethod(String requestMethod) {
        if (HttpConstants.Method.GET.equals(requestMethod)) {
            return true;
        }

        if (HttpConstants.Method.POST.equals(requestMethod)) {
            return true;
        }

        if (HttpConstants.Method.PUT.equals(requestMethod)) {
            return true;
        }

        if (HttpConstants.Method.HEAD.equals(requestMethod)) {
            return true;
        }

        if (HttpConstants.Method.DELETE.equals(requestMethod)) {
            return true;
        }

        return false;
    }

}
