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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class DefaultRouteMatcherTest extends Assert {

    private DefaultRouteMatcher routeMatcher;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        routeMatcher = new DefaultRouteMatcher();
    }

    @After
    public void after() {
        routeMatcher = null;
    }

    @Test
    public void testNullUrlPatternRoute() throws Exception {
        Route route = new Route(null, HttpConstants.Method.GET, new EmptyRouteHandler());
        thrown.expect(Exception.class);
        thrown.expectMessage("The url pattern cannot be null or empty");
        routeMatcher.addRoute(route);
    }

    @Test
    public void testEmptyUrlPatternRoute() throws Exception {
        Route route = new Route("", HttpConstants.Method.GET, new EmptyRouteHandler());
        thrown.expect(Exception.class);
        thrown.expectMessage("The url pattern cannot be null or empty");
        routeMatcher.addRoute(route);
    }

    @Test
    public void testInvalidMethodRequestRoute() throws Exception {
        Route route = new Route("/*", "GETT", new EmptyRouteHandler());
        thrown.expect(Exception.class);
        thrown.expectMessage("Invalid request method");
        routeMatcher.addRoute(route);
    }

    @Test
    public void testInvalidUrlPatternRoute() throws Exception {
        Route route = new Route("$", HttpConstants.Method.GET, new EmptyRouteHandler());
        thrown.expect(Exception.class);
        thrown.expectMessage("Invalid url pattern");
        routeMatcher.addRoute(route);
    }

    @Test
    public void testAddRoute() throws Exception {
        Route route = new Route("/*", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        assertEquals(routeMatcher.getRoutes().size(), 1);
        assertEquals(routeMatcher.getRoutes(HttpConstants.Method.GET).size(), 1);
    }

    @Test
    public void testFindRoutes() throws Exception {
        Route route = new Route("/contact", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact", HttpConstants.Method.GET);
        assertNotNull(routeMatches);
        assertEquals(routeMatches.size(), 1);

        routeMatches = routeMatcher.findRoutes("/contact", HttpConstants.Method.POST);
        assertNotNull(routeMatches);
        assertEquals(routeMatches.size(), 0);

        routeMatches = routeMatcher.findRoutes("/", HttpConstants.Method.GET);
        assertNotNull(routeMatches);
        assertEquals(routeMatches.size(), 0);
    }

    @Test
    public void testPathParamsRoute() throws Exception {
        Route route = new Route("/contact/:id", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact/3", HttpConstants.Method.GET);
        assertEquals(routeMatches.size(), 1);

        Map<String, String> pathParameters = routeMatches.get(0).getPathParameters();
        assertNotNull(pathParameters);
        assertEquals(pathParameters.size(), 1);
        assertTrue(pathParameters.containsKey("id"));
        assertEquals(pathParameters.get("id"), String.valueOf(3));
    }

    @Test
    public void testWildcardRoute() throws Exception {
        Route route = new Route("/*", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact/3", HttpConstants.Method.GET);
        assertEquals(routeMatches.size(), 1);
    }

}
