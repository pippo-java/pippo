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
package ro.pippo.metrics;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RouteTransformer;
import ro.pippo.core.util.LangUtils;
import ro.pippo.core.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author Decebal Suiu
 */
public class MetricsTransformer implements RouteTransformer {

    private static final Logger log = LoggerFactory.getLogger(MetricsTransformer.class);

    private MetricRegistry metricRegistry;

    public MetricsTransformer(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public Route transform(Route route) {
        Method method = route.getAttribute("__controllerMethod");
        if (method == null) {
            try {
                method = route.getRouteHandler().getClass().getMethod("handle", RouteContext.class);
            } catch (NoSuchMethodException e) {
                throw new PippoRuntimeException(e);
            }
        }

        RouteHandler handler = null;
        if (method.isAnnotationPresent(Metered.class)) {
            log.debug("Found '{}' annotation on method '{}'", Metered.class.getSimpleName(), LangUtils.toString(method));
            Metered metered = method.getAnnotation(Metered.class);
            String metricName = !metered.value().isEmpty() ? metered.value() : getMetricName(route, method);
            handler = new MeteredHandler(metricName, metricRegistry, route.getRouteHandler());
        } else if (method.isAnnotationPresent(Timed.class)) {
            log.debug("Found '{}' annotation on method '{}'", Timed.class.getSimpleName(), LangUtils.toString(method));
            Timed timed = method.getAnnotation(Timed.class);
            String metricName = !timed.value().isEmpty() ? timed.value() : getMetricName(route, method);
            handler = new TimedHandler(metricName, metricRegistry, route.getRouteHandler());
        } else if (method.isAnnotationPresent(Counted.class)) {
            log.debug("Found '{}' annotation on method '{}'", Counted.class.getSimpleName(), LangUtils.toString(method));
            Counted counted = method.getAnnotation(Counted.class);
            String metricName = !counted.value().isEmpty() ? counted.value() : getMetricName(route, method);
            handler = new CountedHandler(metricName, counted.active(), metricRegistry, route.getRouteHandler());
        }

        if (handler != null) {
            route.setRouteHandler(handler);
        }

        return route;
    }

    private String getMetricName(Route route, Method method) {
        String metricName = route.getName();
        if (StringUtils.isNullOrEmpty(metricName)) {
            metricName = MetricRegistry.name(method.getDeclaringClass(), method.getName());
        }

        return metricName;
    }

}
