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
package ro.pippo.core.route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A compiled route contains derived information from a route object plus
 * additional one (regex, pattern, parameterNames).
 * The base route is immutable (you cannot modify it) but you can modify the information
 * from the compiled route (name, runAsFinally, routeHandler).
 * The perfect approach to modify a compiled route is via {@link CompiledRouteTransformer}.
 *
 * @author Decebal Suiu
 */
public class CompiledRoute extends Route {

    private final Route route;

    // additional metadata
    private final String regex;
    private final Pattern pattern;
    private final List<String> parameterNames;

    public CompiledRoute(Route route, String regex, List<String> parameterNames) {
        super(route.getRequestMethod(), route.getUriPattern(), route.getRouteHandler());

        this.name = route.name;
        this.runAsFinally = route.isRunAsFinally();
        this.attributes = new HashMap<>(route.getAttributes());

        // set additional metadata
        this.route = route;
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.parameterNames = parameterNames;
    }

    public Route getRoute() {
        return route;
    }

    public void setRouteHandler(RouteHandler routeHandler) {
        this.routeHandler = routeHandler;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getRegex() {
        return regex;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

}
