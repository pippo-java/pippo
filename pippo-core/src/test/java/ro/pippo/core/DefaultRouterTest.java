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
package ro.pippo.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ro.pippo.core.route.DefaultRouter;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteMatch;
import ro.pippo.core.route.WebjarsResourceHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Decebal Suiu
 */
public class DefaultRouterTest extends Assert {

    private DefaultRouter router;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        router = new DefaultRouter();
    }

    @After
    public void after() {
        router = null;
    }

    @Test
    public void testNullUriPatternRoute() throws Exception {
        Route route = new Route(null, HttpConstants.Method.GET, new EmptyRouteHandler());
        thrown.expect(Exception.class);
        thrown.expectMessage("The uri pattern cannot be null or empty");
        router.addRoute(route);
    }

    @Test
    public void testEmptyUriPatternRoute() throws Exception {
        Route route = new Route("", HttpConstants.Method.GET, new EmptyRouteHandler());
        thrown.expect(Exception.class);
        thrown.expectMessage("The uri pattern cannot be null or empty");
        router.addRoute(route);
    }

    @Test
    public void testUnspecifiedMethodRequestRoute() throws Exception {
        Route route = new Route("/.*", "", new EmptyRouteHandler());
        thrown.expect(Exception.class);
        thrown.expectMessage("Unspecified request method");
        router.addRoute(route);
    }

    @Test
    public void testAddRoute() throws Exception {
        Route route = new Route("/.*", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        assertEquals(1, router.getRoutes().size());
        assertEquals(1, router.getRoutes(HttpConstants.Method.GET).size());
    }

    @Test
    public void testRemoveRoute() throws Exception {
        Route route = new Route("/.*", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        assertEquals(1, router.getRoutes().size());
        assertEquals(1, router.getRoutes(HttpConstants.Method.GET).size());

        router.removeRoute(route);
        assertEquals(0, router.getRoutes().size());
        assertEquals(0, router.getRoutes(HttpConstants.Method.GET).size());
    }

    @Test
    public void testFindRoutes() throws Exception {
        Route route = new Route("/contact", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        List<RouteMatch> routeMatches = router.findRoutes("/contact", HttpConstants.Method.GET);
        assertNotNull(routeMatches);
        assertEquals(1, routeMatches.size());

        routeMatches = router.findRoutes("/contact", HttpConstants.Method.POST);
        assertNotNull(routeMatches);
        assertEquals(0, routeMatches.size());

        routeMatches = router.findRoutes("/", HttpConstants.Method.GET);
        assertNotNull(routeMatches);
        assertEquals(0, routeMatches.size());
    }

    @Test
    public void testPathParamsRoute() throws Exception {
        Route route = new Route("/contact/{id}", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        List<RouteMatch> routeMatches = router.findRoutes("/contact/3", HttpConstants.Method.GET);
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
        router.addRoute(route);

        List<RouteMatch> routeMatches = router.findRoutes("/contact/3", HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());
    }

    @Test
    public void testPatchRoute() throws Exception {
        Route route = new Route("/contact/{id}", HttpConstants.Method.PATCH, new EmptyRouteHandler());
        router.addRoute(route);

        List<RouteMatch> routeMatches = router.findRoutes("/contact/3", HttpConstants.Method.PATCH);
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
        router.addRoute(route);

        List<RouteMatch> routeMatches = router.findRoutes("/contact/3", HttpConstants.Method.PATCH);
        assertEquals(1, routeMatches.size());

        Map<String, String> pathParameters = routeMatches.get(0).getPathParameters();
        assertNotNull(pathParameters);
        assertEquals(1, pathParameters.size());
        assertTrue(pathParameters.containsKey("id"));
        assertEquals(String.valueOf(3), pathParameters.get("id"));

        routeMatches = router.findRoutes("/contact/a", HttpConstants.Method.PATCH);
        assertEquals(0, routeMatches.size());
    }

    @Test
    public void testIntIdRoute2() throws Exception {
        Route route = new Route("/contact/{id: [0-9]+}/something/{else: [A-z]*}", HttpConstants.Method.GET,
            new EmptyRouteHandler());
        router.addRoute(route);

        List<RouteMatch> routeMatches = router.findRoutes("/contact/3/something/borrowed", HttpConstants.Method.GET);
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
        Route route = new Route(webjars.getUriPattern(), HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        List<RouteMatch> routeMatches = router.findRoutes("/webjars/bootstrap/3.0.2/css/bootstrap.min.css",
            HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());
    }

    @Test
    public void testParameters() throws Exception {
        // /////////////////////////////////////////////////////////////////////
        // One parameter:
        // /////////////////////////////////////////////////////////////////////
        router.addRoute(new Route("/{name}/dashboard", HttpConstants.Method.GET, new EmptyRouteHandler()));

        assertEquals(0, router.findRoutes("/dashboard", HttpConstants.Method.GET).size());

        List<RouteMatch> matches = router.findRoutes("/John/dashboard", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        assertEquals("John", matches.get(0).getPathParameters().get("name"));

        // /////////////////////////////////////////////////////////////////////
        // More parameters
        // /////////////////////////////////////////////////////////////////////
        router.addRoute(new Route("/{name}/{id}/dashboard", HttpConstants.Method.GET, new EmptyRouteHandler()));

        assertEquals(0, router.findRoutes("/dashboard", HttpConstants.Method.GET).size());

        matches = router.findRoutes("/John/20/dashboard", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        assertEquals("John", matches.get(0).getPathParameters().get("name"));
        assertEquals("20", matches.get(0).getPathParameters().get("id"));
    }

    @Test
    public void testParametersAndRegex() throws Exception {
        router.addRoute(new Route("/John/{id}/.*", HttpConstants.Method.GET, new EmptyRouteHandler()));

        List<RouteMatch> matches = router.findRoutes("/John/20/dashboard", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        RouteMatch match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("20", match.getPathParameters().get("id"));

        matches = router.findRoutes("/John/20/admin", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("20", match.getPathParameters().get("id"));

        matches = router.findRoutes("/John/20/mock", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("20", match.getPathParameters().get("id"));
    }

    @Test
    public void testParametersAndRegexInsideVariableParts() throws Exception {
        router.addRoute(new Route("/public/{path: .*}", HttpConstants.Method.GET, new EmptyRouteHandler()));

        String pathUnderTest = "/public/css/app.css";
        List<RouteMatch> matches = router.findRoutes(pathUnderTest, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        RouteMatch match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("css/app.css", match.getPathParameters().get("path"));

        pathUnderTest = "/public/js/main.js";
        matches = router.findRoutes(pathUnderTest, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("js/main.js", match.getPathParameters().get("path"));

        pathUnderTest = "/public/robots.txt";
        matches = router.findRoutes(pathUnderTest, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("robots.txt", match.getPathParameters().get("path"));

        // multiple parameter parsing with regex expressions
        router.addRoute(new Route("/{name: .+}/photos/{id: [0-9]+}", HttpConstants.Method.GET, new EmptyRouteHandler()));

        pathUnderTest = "/John/photos/2201";
        matches = router.findRoutes(pathUnderTest, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        match = matches.get(0);
        assertEquals(2, match.getPathParameters().size());
        assertEquals("John", match.getPathParameters().get("name"));
        assertEquals("2201", match.getPathParameters().get("id"));

        assertEquals(0, router.findRoutes("John/photos/first", HttpConstants.Method.GET).size());
    }

    @Test
    public void testParametersDontCrossSlashes() throws Exception {
        router.addRoute(new Route("/blah/{id}/{id2}/{id3}/morestuff/at/the/end", HttpConstants.Method.GET,
            new EmptyRouteHandler()));

        // this must match
        assertEquals(1, router.findRoutes("/blah/id/id2/id3/morestuff/at/the/end", HttpConstants.Method.GET).size());

        // this should not match as the last "end" is missing
        assertEquals(0, router.findRoutes("/blah/id/id2/id3/morestuff/at/the", HttpConstants.Method.GET).size());
    }

    @Test
    public void testPointsInRegexDontCrashRegexInTheMiddleOfTheRoute() throws Exception {
        router.addRoute(new Route("/blah/{id}/myname", HttpConstants.Method.GET, new EmptyRouteHandler()));

        // the "." in the route should not make any trouble:
        String routeFromServer = "/blah/my.id/myname";

        List<RouteMatch> matches = router.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        RouteMatch match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("my.id", match.getPathParameters().get("id"));

        // and another slightly different route
        routeFromServer = "/blah/my.id/myname/should_not_match";
        matches = router.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(0, matches.size());
    }

    @Test
    public void testPointsInRegexDontCrashRegexAtEnd() throws Exception {
        router.addRoute(new Route("/blah/{id}", HttpConstants.Method.GET, new EmptyRouteHandler()));

        // the "." in the route should not make any trouble:
        // even if it's the last part of the route
        List<RouteMatch> matches = router.findRoutes("/blah/my.id", HttpConstants.Method.GET);
        assertEquals(1, matches.size());
        RouteMatch match = matches.get(0);
        assertEquals(1, match.getPathParameters().size());
        assertEquals("my.id", match.getPathParameters().get("id"));
    }

    @Test
    public void testRegexInRouteWorksWithEscapes() throws Exception {
        // Test escaped constructs in regex
        // regex with escaped construct in a route
        router.addRoute(new Route("/customers/\\d+", HttpConstants.Method.GET, new EmptyRouteHandler()));

        assertEquals(1, router.findRoutes("/customers/1234", HttpConstants.Method.GET).size());
        assertEquals(0, router.findRoutes("/customers/12ab", HttpConstants.Method.GET).size());

        // regex with escaped construct in a route with variable parts
        router = new DefaultRouter();
        router.addRoute(new Route("/customers/{id: \\d+}", HttpConstants.Method.GET, new EmptyRouteHandler()));

        assertEquals(1, router.findRoutes("/customers/1234", HttpConstants.Method.GET).size());
        assertEquals(0, router.findRoutes("/customers/12x", HttpConstants.Method.GET).size());

        RouteMatch routeMatch = router.findRoutes("/customers/1234", HttpConstants.Method.GET).get(0);
        Map<String, String> map = routeMatch.getPathParameters();
        assertEquals(1, map.size());
        assertEquals("1234", map.get("id"));
    }

    @Test
    public void testRegexInRouteWorksWithoutSlashAtTheEnd() throws Exception {
        Route route = new Route("/blah/{id}/.*", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        // the "." in the real route should work without any problems:
        String routeFromServer = "/blah/my.id/and/some/more/stuff";

        List<RouteMatch> routeMatches = router.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());
        RouteMatch routeMatch = routeMatches.get(0);
        assertEquals(route, routeMatch.getRoute());

        assertEquals(1, routeMatch.getPathParameters().size());
        assertEquals("my.id", routeMatch.getPathParameters().get("id"));

        // another slightly different route.
        routeFromServer = "/blah/my.id/";

        routeMatches = router.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());
        routeMatch = routeMatches.get(0);
        assertEquals(route, routeMatch.getRoute());

        assertEquals(1, routeMatch.getPathParameters().size());
        assertEquals("my.id", routeMatch.getPathParameters().get("id"));

        routeMatches = router.findRoutes("/blah/my.id", HttpConstants.Method.GET);
        assertEquals(0, routeMatches.size());
    }

    @Test
    public void testRouteWithUrlEncodedSlashGetsChoppedCorrectly() throws Exception {
        Route route = new Route("/blah/{id}/.*", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        // Just a simple test to make sure everything works on a not encoded
        // uri: decoded this would be /blah/my/id/and/some/more/stuff
        String routeFromServer = "/blah/my%2fid/and/some/more/stuff";

        List<RouteMatch> routeMatches = router.findRoutes(routeFromServer, HttpConstants.Method.GET);
        assertEquals(1, routeMatches.size());
        RouteMatch routeMatch = routeMatches.get(0);
        assertEquals(route, routeMatch.getRoute());

        assertEquals(1, routeMatch.getPathParameters().size());
        assertEquals("my%2fid", routeMatch.getPathParameters().get("id"));
    }

    @Test
    public void testUriForWithRegex() throws Exception {
        Route route = new Route("/user/{email}/{id: .*}", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("email", "test@test.com");
        parameters.put("id", 5);
        String path = router.uriFor(route.getUriPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com/5"));
    }

    @Test
    public void testUriForWithMultipleRegex() throws Exception {
        Route route = new Route("/user/{email: .*}/test/{id: .*}", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("email", "test@test.com");
        parameters.put("id", 5);
        String path = router.uriFor(route.getUriPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com/test/5"));
    }

    @Test
    public void testUriForWithSplat() throws Exception {
        Route route = new Route("/repository/{repo: .*}/ticket/{id: .*}", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("repo", "test/myrepo");
        parameters.put("id", 5);
        String path = router.uriFor(route.getUriPattern(), parameters);

        assertThat(path, equalTo("/repository/test/myrepo/ticket/5"));

        List<RouteMatch> matches = router.findRoutes("/repository/test/myrepo/ticket/5", HttpConstants.Method.GET);

        assertFalse(matches.isEmpty());
        assertEquals("test/myrepo", matches.get(0).getPathParameters().get("repo"));
        assertEquals("5", matches.get(0).getPathParameters().get("id"));
    }

    @Test
    public void testUriForWithRegexAndQueryParameters() throws Exception {
        Route route = new Route("/user/{email}/{id: .*}", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", "test@test.com");
        parameters.put("id", 5);
        parameters.put("query", "recent_changes");
        String path = router.uriFor(route.getUriPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com/5?query=recent_changes"));
    }

    @Test
    public void testUriForWithEncodedParameters() throws Exception {
        Route route = new Route("/user/{email}", HttpConstants.Method.GET, new EmptyRouteHandler());
        router.addRoute(route);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", "test@test.com");
        parameters.put("name", "Decebal Suiu");
        String path = router.uriFor(route.getUriPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com?name=Decebal%20Suiu"));
    }

    @Test
    public void testExclusionFilter() throws Exception {
        Route route = new Route("^(?!/(webjars|public)/).*", HttpConstants.Method.ALL, new EmptyRouteHandler());
        router.addRoute(route);

        List<RouteMatch> matches = router.findRoutes("/test/route", HttpConstants.Method.GET);
        assertEquals(1, matches.size());

        matches = router.findRoutes("/webjars/route", HttpConstants.Method.GET);
        assertEquals(0, matches.size());

        matches = router.findRoutes("/public/route", HttpConstants.Method.GET);
        assertEquals(0, matches.size());
    }

}
