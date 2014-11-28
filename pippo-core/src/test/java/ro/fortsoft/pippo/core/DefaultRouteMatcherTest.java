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
package ro.fortsoft.pippo.core;

import ro.fortsoft.pippo.core.route.DefaultRouteMatcher;
import ro.fortsoft.pippo.core.route.Route;
import ro.fortsoft.pippo.core.route.RouteMatch;
import ro.fortsoft.pippo.core.route.WebjarsResourceHandler;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        Route route = new Route("/.*", "GETT", new EmptyRouteHandler());
        thrown.expect(Exception.class);
        thrown.expectMessage("Invalid request method");
        routeMatcher.addRoute(route);
    }

    @Test
    public void testAddRoute() throws Exception {
        Route route = new Route("/.*", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        assertEquals(1, routeMatcher.getRoutes().size());
        assertEquals(1, routeMatcher.getRoutes(HttpConstants.Method.GET).size());
    }

    @Test
    public void testFindRoutes() throws Exception {
        Route route = new Route("/contact", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact", HttpConstants.Method.GET);
        assertNotNull(routeMatches);
        assertEquals(1, routeMatches.size());

        routeMatches = routeMatcher.findRoutes("/contact", HttpConstants.Method.POST);
        assertNotNull(routeMatches);
        assertEquals(0, routeMatches.size());

        routeMatches = routeMatcher.findRoutes("/", HttpConstants.Method.GET);
        assertNotNull(routeMatches);
        assertEquals(0, routeMatches.size());
    }

    @Test
    public void testPathParamsRoute() throws Exception {
        Route route = new Route("/contact/{id}", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact/3", HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());

        Map<String, String> pathParameters = routeMatches.get(0).getPathParameters();
        assertNotNull(pathParameters);
        assertEquals(1, pathParameters.size());
        assertTrue(pathParameters.containsKey("id"));
        assertEquals(String.valueOf(3), pathParameters.get("id"));
    }

    @Test
    public void testWildcardRoute() throws Exception {
        Route route = new Route("/.*", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact/3", HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());
    }

    @Test
    public void testPatchRoute() throws Exception {
        Route route = new Route("/contact/{id}", HttpConstants.Method.PATCH, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact/3", HttpConstants.Method.PATCH);
        assertEquals(1, routeMatches.size());

        Map<String, String> pathParameters = routeMatches.get(0).getPathParameters();
        assertNotNull(pathParameters);
        assertEquals(1, pathParameters.size());
        assertTrue(pathParameters.containsKey("id"));
        assertEquals(String.valueOf(3), pathParameters.get("id"));
    }

    @Test
    public void testIntIdRoute() throws Exception {
        Route route = new Route("/contact/{id: [0-9]+}", HttpConstants.Method.PATCH, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact/3", HttpConstants.Method.PATCH);
        assertEquals(1, routeMatches.size());

        Map<String, String> pathParameters = routeMatches.get(0).getPathParameters();
        assertNotNull(pathParameters);
        assertEquals(1, pathParameters.size());
        assertTrue(pathParameters.containsKey("id"));
        assertEquals(String.valueOf(3), pathParameters.get("id"));

        routeMatches = routeMatcher.findRoutes("/contact/a", HttpConstants.Method.PATCH);
        assertEquals(0, routeMatches.size());

    }

    @Test
    public void testIntIdRoute2() throws Exception {
        Route route = new Route("/contact/{id: [0-9]+}/something/{else: [A-z]*}", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact/3/something/borrowed", HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());

        Map<String, String> pathParameters = routeMatches.get(0).getPathParameters();
        assertNotNull(pathParameters);
        assertEquals(2, pathParameters.size());
        assertTrue(pathParameters.containsKey("id"));
        assertEquals(String.valueOf(3), pathParameters.get("id"));
        assertEquals("borrowed", pathParameters.get("else"));

    }

    @Test
    public void testWebjarsRoute() throws Exception {
        WebjarsResourceHandler webjars = new WebjarsResourceHandler();
        Route route = new Route(webjars.getUrlPattern(), HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/webjars/bootstrap/3.0.2/css/bootstrap.min.css", HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());

    }
}
