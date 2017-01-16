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
package ro.pippo.controller;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteTransformer;

import java.lang.reflect.Method;

/**
 * @author Decebal Suiu
 */
@MetaInfServices
public class NameTransformer implements RouteTransformer {

    private static final Logger log = LoggerFactory.getLogger(NameTransformer.class);

    @Override
    public Route transform(Route route) {
        Method method = route.getAttribute("__controllerMethod");
        if (method == null) {
            // it's not a controller route; do nothing
            return route;
        }

        if (method.isAnnotationPresent(Named.class)) {
            Named named = method.getAnnotation(Named.class);
            String name = named.value();
            log.debug("Set name '{}' for route {} '{}'", name, route.getRequestMethod(), route.getUriPattern());
            route.setName(name);
        }

        return route;
    }

}
