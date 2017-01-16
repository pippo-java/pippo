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

/**
 * A {@code RouteTransformer} transform a route.
 * For example you can modify the route name or the route handler.
 * If the returned route si null then the route will be removed from the route list,
 * so the route will not be added to router.
 *
 * @author Decebal Suiu
 */
public interface RouteTransformer {

    /**
     * Transform the supplied route and return a new replacement route.
     * If you want to disable/remove the supplied route you can returns null.
     *
     * @param route
     * @return a transformed route or null
     */
    Route transform(Route route);

}
