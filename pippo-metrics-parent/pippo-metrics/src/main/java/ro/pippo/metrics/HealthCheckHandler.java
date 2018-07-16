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

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import ro.pippo.core.Response;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

import java.util.SortedMap;

/**
 * Returns a response with following status code:
 * <ul>
 * <li>501 (not implemented) if the registry is empty (no health checks)</li>
 * <li>200 (ok) if all the health checks are healthy</li>
 * <li>500 (internal error) otherwise</li>
 * </ul>
 *
 * @author Decebal Suiu
 */
public class HealthCheckHandler implements RouteHandler {

    final HealthCheckRegistry healthCheckRegistry;

    public HealthCheckHandler(HealthCheckRegistry healthCheckRegistry) {
        this.healthCheckRegistry = healthCheckRegistry;
    }

    @Override
    public void handle(RouteContext routeContext) {
        Response response = routeContext.getResponse().noCache().text();

        SortedMap<String, HealthCheck.Result> healthChecks = healthCheckRegistry.runHealthChecks();
        if (healthChecks.isEmpty()) {
            response.notImplemented().send("The health checks are empty");
        } else {
            boolean notHealthy = healthChecks.values().stream().anyMatch(hc -> !hc.isHealthy());
            if (notHealthy) {
                response.internalError().send("The health is bad");
            } else {
                response.ok().send("The health is good");
            }
        }
    }

}
