/*
 * Copyright (C) 2015 the original author or authors.
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
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.route.RoutePostDispatchListener;
import ro.pippo.core.route.RoutePreDispatchListener;

/**
 * Collects general {@link Request} and {@link Response} metrics.
 *
 * @author James Moger
 */
public class MetricsDispatchListener implements RoutePreDispatchListener, RoutePostDispatchListener {

    protected Meter allRequestsMeter;
    protected Counter activeRequests;
    protected Meter badRequests;
    protected Meter unauthorizedRequests;
    protected Meter forbiddenRequests;
    protected Meter routesNotFound;
    protected Meter conflictRequests;
    protected Meter internalServerErrors;

    public MetricsDispatchListener(MetricRegistry metricRegistry) {
        // general request metrics
        allRequestsMeter = metricRegistry.meter("dispatcher.requests.allRequests");
        activeRequests = metricRegistry.counter("dispatcher.requests.activeRequests");

        // response code metrics
        badRequests = metricRegistry.meter("dispatcher.requests.400BadRequests");
        unauthorizedRequests = metricRegistry.meter("dispatcher.requests.401Unauthorized");
        forbiddenRequests = metricRegistry.meter("dispatcher.requests.403Forbidden");
        routesNotFound = metricRegistry.meter("dispatcher.requests.404NotFound");
        conflictRequests = metricRegistry.meter("dispatcher.requests.409Conflict");
        internalServerErrors = metricRegistry.meter("dispatcher.requests.500InternalError");
    }

    @Override
    public void onPreDispatch(Request request, Response response) {
        activeRequests.inc();
    }

    @Override
    public void onPostDispatch(Request request, Response response) {
        updateStatusCodeMetrics(response);
        activeRequests.dec();
    }

    protected void updateStatusCodeMetrics(Response response) {
        allRequestsMeter.mark();
        switch (response.getStatus()) {
            case HttpConstants.StatusCode.BAD_REQUEST:
                badRequests.mark();
                break;
            case HttpConstants.StatusCode.UNAUTHORIZED:
                unauthorizedRequests.mark();
                break;
            case HttpConstants.StatusCode.FORBIDDEN:
                forbiddenRequests.mark();
                break;
            case HttpConstants.StatusCode.NOT_FOUND:
                routesNotFound.mark();
                break;
            case HttpConstants.StatusCode.CONFLICT:
                conflictRequests.mark();
                break;
            case HttpConstants.StatusCode.INTERNAL_ERROR:
                internalServerErrors.mark();
                break;
            default:
                break;
        }
    }

}
