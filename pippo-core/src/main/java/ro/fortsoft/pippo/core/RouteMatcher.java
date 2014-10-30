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
package ro.fortsoft.pippo.core;

import java.util.List;

/**
 * This class allows you to do route requests based on the HTTP verb (request method) and the request URI,
 * in a manner similar to Sinatra or Express.
 * Routes are matched in the order they are added/defined.
 *
 * @author Decebal Suiu
 */
public interface RouteMatcher {

    public void addRoute(Route route) throws Exception;

    public List<RouteMatch> findRoutes(String requestMethod, String requestUri);

    public List<Route> getRoutes();

}
