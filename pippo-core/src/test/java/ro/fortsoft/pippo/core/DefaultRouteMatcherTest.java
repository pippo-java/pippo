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

import static org.hamcrest.CoreMatchers.equalTo;

import ro.fortsoft.pippo.core.route.DefaultRouter;
import ro.fortsoft.pippo.core.route.Route;
import ro.fortsoft.pippo.core.route.RouteMatch;
import ro.fortsoft.pippo.core.route.WebjarsResourceHandler;

import java.util.HashMap;
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

    private DefaultRouter routeMatcher;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        routeMatcher = new DefaultRouter();
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
        Route route = new Route("/contact/{id: [0-9]+}/something/{else: [A-z]*}", HttpConstants.Method.GET,
                new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/contact/3/something/borrowed",
                HttpConstants.Method.GET);
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

        List<RouteMatch> routeMatches = routeMatcher.findRoutes("/webjars/bootstrap/3.0.2/css/bootstrap.min.css",
                HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());

    }

    @Test
    public void testParameters() throws Exception {

        // /////////////////////////////////////////////////////////////////////
        // One parameter:
        // /////////////////////////////////////////////////////////////////////
        routeMatcher.addRoute(new Route("/{name}/dashboard", HttpConstants.Method.GET, new EmptyRouteHandler()));

        assertEquals(0, routeMatcher.findRoutes("/dashboard", HttpConstants.Method.GET).size());

        List<RouteMatch> matches = routeMatcher.findRoutes("/John/dashboard", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        assertEquals("John", matches.get(0).getPathParameters().get("name"));

        // /////////////////////////////////////////////////////////////////////
        // More parameters
        // /////////////////////////////////////////////////////////////////////
        routeMatcher.addRoute(new Route("/{name}/{id}/dashboard", HttpConstants.Method.GET, new EmptyRouteHandler()));

        assertEquals(0, routeMatcher.findRoutes("/dashboard", HttpConstants.Method.GET).size());

        matches = routeMatcher.findRoutes("/John/20/dashboard", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        assertEquals("John", matches.get(0).getPathParameters().get("name"));
        assertEquals("20", matches.get(0).getPathParameters().get("id"));
    }

    @Test
    public void testParametersAndRegex() throws Exception {

        routeMatcher.addRoute(new Route("/John/{id}/.*", HttpConstants.Method.GET, new EmptyRouteHandler()));

        List<RouteMatch> matches = routeMatcher.findRoutes("/John/20/dashboard", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        RouteMatch match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("20", match.getPathParameters().get("id"));

        matches = routeMatcher.findRoutes("/John/20/admin", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("20", match.getPathParameters().get("id"));

        matches = routeMatcher.findRoutes("/John/20/mock", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("20", match.getPathParameters().get("id"));

    }

    @Test
    public void testParametersAndRegexInsideVariableParts() throws Exception {

        routeMatcher.addRoute(new Route("/public/{path: .*}", HttpConstants.Method.GET, new EmptyRouteHandler()));

        String pathUnderTest = "/public/css/app.css";
        List<RouteMatch> matches = routeMatcher.findRoutes(pathUnderTest, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        RouteMatch match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("css/app.css", match.getPathParameters().get("path"));

        pathUnderTest = "/public/js/main.js";
        matches = routeMatcher.findRoutes(pathUnderTest, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("js/main.js", match.getPathParameters().get("path"));

        pathUnderTest = "/public/robots.txt";
        matches = routeMatcher.findRoutes(pathUnderTest, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("robots.txt", match.getPathParameters().get("path"));

        // multiple parameter parsing with regex expressions
        routeMatcher.addRoute(new Route("/{name: .+}/photos/{id: [0-9]+}", HttpConstants.Method.GET,
                new EmptyRouteHandler()));

        pathUnderTest = "/John/photos/2201";
        matches = routeMatcher.findRoutes(pathUnderTest, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(2, match.getPathParameters().size());
        assertEquals("John", match.getPathParameters().get("name"));
        assertEquals("2201", match.getPathParameters().get("id"));

        assertEquals(0, routeMatcher.findRoutes("John/photos/first", HttpConstants.Method.GET).size());
    }

    @Test
    public void testParametersDontCrossSlashes() throws Exception {
        routeMatcher.addRoute(new Route("/blah/{id}/{id2}/{id3}/morestuff/at/the/end", HttpConstants.Method.GET,
                new EmptyRouteHandler()));

        // this must match
        assertEquals(1, routeMatcher.findRoutes("/blah/id/id2/id3/morestuff/at/the/end", HttpConstants.Method.GET)
                .size());

        // this should not match as the last "end" is missing
        assertEquals(0, routeMatcher.findRoutes("/blah/id/id2/id3/morestuff/at/the", HttpConstants.Method.GET).size());
    }

    @Test
    public void testPointsInRegexDontCrashRegexInTheMiddleOfTheRoute() throws Exception {
        routeMatcher.addRoute(new Route("/blah/{id}/myname", HttpConstants.Method.GET, new EmptyRouteHandler()));

        // the "." in the route should not make any trouble:
        String routeFromServer = "/blah/my.id/myname";

        List<RouteMatch> matches = routeMatcher.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        RouteMatch match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("my.id", match.getPathParameters().get("id"));

        // and another slightly different route
        routeFromServer = "/blah/my.id/myname/should_not_match";
        matches = routeMatcher.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(0, matches.size());
    }

    @Test
    public void testPointsInRegexDontCrashRegexAtEnd() throws Exception {
        routeMatcher.addRoute(new Route("/blah/{id}", HttpConstants.Method.GET, new EmptyRouteHandler()));

        // the "." in the route should not make any trouble:
        // even if it's the last part of the route
        List<RouteMatch> matches = routeMatcher.findRoutes("/blah/my.id", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        RouteMatch match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("my.id", match.getPathParameters().get("id"));
    }

    @Test
    public void testRegexInRouteWorksWithEscapes() throws Exception {
        // Test escaped constructs in regex
        // regex with escaped construct in a route
        routeMatcher.addRoute(new Route("/customers/\\d+", HttpConstants.Method.GET, new EmptyRouteHandler()));

        assertEquals(1, routeMatcher.findRoutes("/customers/1234", HttpConstants.Method.GET).size());
        assertEquals(0, routeMatcher.findRoutes("/customers/12ab", HttpConstants.Method.GET).size());

        // regex with escaped construct in a route with variable parts
        routeMatcher = new DefaultRouter();
        routeMatcher.addRoute(new Route("/customers/{id: \\d+}", HttpConstants.Method.GET, new EmptyRouteHandler()));

        assertEquals(1, routeMatcher.findRoutes("/customers/1234", HttpConstants.Method.GET).size());
        assertEquals(0, routeMatcher.findRoutes("/customers/12x", HttpConstants.Method.GET).size());

        RouteMatch routeMatch = routeMatcher.findRoutes("/customers/1234", HttpConstants.Method.GET).get(0);
        Map<String, String> map = routeMatch.getPathParameters();
        assertEquals(1, map.size());
        assertEquals("1234", map.get("id"));
    }

    @Test
    public void testRegexInRouteWorksWithoutSlashAtTheEnd() throws Exception {
        Route route = new Route("/blah/{id}/.*", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        // the "." in the real route should work without any problems:
        String routeFromServer = "/blah/my.id/and/some/more/stuff";

        List<RouteMatch> routeMatches = routeMatcher.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());
        RouteMatch routeMatch = routeMatches.get(0);
        assertEquals(route, routeMatch.getRoute());

        assertEquals(1, routeMatch.getPathParameters().size());
        assertEquals("my.id", routeMatch.getPathParameters().get("id"));

        // another slightly different route.
        routeFromServer = "/blah/my.id/";

        routeMatches = routeMatcher.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());
        routeMatch = routeMatches.get(0);
        assertEquals(route, routeMatch.getRoute());

        assertEquals(1, routeMatch.getPathParameters().size());
        assertEquals("my.id", routeMatch.getPathParameters().get("id"));

        routeMatches = routeMatcher.findRoutes("/blah/my.id", HttpConstants.Method.GET);
        assertEquals(0, routeMatches.size());
    }

    @Test
    public void testRouteWithUrlEncodedSlashGetsChoppedCorrectly() throws Exception {

        Route route = new Route("/blah/{id}/.*", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        // Just a simple test to make sure everything works on a not encoded
        // uri: decoded this would be /blah/my/id/and/some/more/stuff
        String routeFromServer = "/blah/my%2fid/and/some/more/stuff";

        List<RouteMatch> routeMatches = routeMatcher.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());
        RouteMatch routeMatch = routeMatches.get(0);
        assertEquals(route, routeMatch.getRoute());

        assertEquals(1, routeMatch.getPathParameters().size());
        assertEquals("my%2fid", routeMatch.getPathParameters().get("id"));

    }

    @Test
    public void testUrlForWithRegex() throws Exception {
        Route route = new Route("/user/{email}/{id: .*}", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("email", "test@test.com");
        parameters.put("id", 5);
        String path = routeMatcher.urlFor(route.getUrlPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com/5"));

    }

    @Test
    public void testUrlForWithRegexAndQueryParameters() throws Exception {
        Route route = new Route("/user/{email}/{id: .*}", HttpConstants.Method.GET, new EmptyRouteHandler());
        routeMatcher.addRoute(route);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("email", "test@test.com");
        parameters.put("id", 5);
        parameters.put("query", "recent_changes");
        String path = routeMatcher.urlFor(route.getUrlPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com/5?query=recent_changes"));
    }
}
