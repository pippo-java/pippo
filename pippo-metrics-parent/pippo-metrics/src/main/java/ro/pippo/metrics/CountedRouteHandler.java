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
package ro.pippo.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

/**
 * @author James Moger
 */
public class CountedRouteHandler implements RouteHandler {

    final String counterName;
    final boolean isActive;
    final RouteHandler routeHandler;
    final MetricRegistry metricRegistry;

    public CountedRouteHandler(String counterName, boolean isActive, RouteHandler routeHandler, MetricRegistry metricRegistry) {
        this.counterName = counterName;
        this.isActive = isActive;
        this.routeHandler = routeHandler;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void handle(RouteContext routeContext) {
        Counter counter = metricRegistry.counter(counterName);
        counter.inc();

        try {
            routeHandler.handle(routeContext);
        } finally {
            if (isActive) {
                counter.dec();
            }
        }
    }

}
