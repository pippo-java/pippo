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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import ro.pippo.core.Application;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

/**
 * It's a decorator route handler that add a {@link Timer} metric.
 *
 * @author James Moger
 */
public class TimedHandler implements RouteHandler {

    final String timerName;
    final MetricRegistry metricRegistry;
    final RouteHandler routeHandler;

    /**
     * This constructor uses {@link SharedMetricRegistries#getDefault()} as metric registry.
     * The default (global) metric registry is set in {@link MetricsInitializer#init(Application)}.
     */
    public TimedHandler(String timerName, RouteHandler routeHandler) {
        this(timerName, SharedMetricRegistries.getDefault(), routeHandler);
    }

    public TimedHandler(String timerName, MetricRegistry metricRegistry, RouteHandler routeHandler) {
        this.timerName = timerName;
        this.metricRegistry = metricRegistry;
        this.routeHandler = routeHandler;
    }

    @Override
    public void handle(RouteContext routeContext) {
        Timer.Context timerContext = metricRegistry.timer(timerName).time();

        try {
            routeHandler.handle(routeContext);
        } finally {
            timerContext.stop();
        }
    }

}
