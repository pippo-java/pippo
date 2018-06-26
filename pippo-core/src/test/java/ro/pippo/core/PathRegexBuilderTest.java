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
package ro.pippo.core;

import org.junit.Before;
import org.junit.Test;
import ro.pippo.core.route.DefaultRouter;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.util.PathRegexBuilder;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Decebal Suiu
 */
public class PathRegexBuilderTest {

    private static final RouteHandler emptyHandler = new EmptyHandler();

    private DefaultRouter router;

    @Before
    public void before() {
        router = new DefaultRouter();
    }

    @Test
    public void testInclude() throws Exception {
        String uri = "/admin";

        String regex = new PathRegexBuilder()
            .includes(
                "/admin"
            )
            .excludes(
                "/admin/login",
                "/webjars",
                "/public"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        assertEquals(1, router.findRoutes("GET", uri).size());
    }

    @Test
    public void testIncludeAnt() throws Exception {
        String uri = "/aaa";

        String regex = new PathRegexBuilder()
            .includes(
                "/{id}"
            )
            .excludes(
                "/admin"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        assertEquals(1, router.findRoutes("GET", uri).size());
    }

    @Test
    public void testMultiIncludeAnt() throws Exception {
        String uri = "/aaa";

        String regex = new PathRegexBuilder()
            .includes(
                "/{id}",
                "/{name}"
            )
            .excludes(
                "/admin"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        assertEquals(1, router.findRoutes("GET", uri).size());
    }

    @Test
    public void testExclude() {
        String uri = "/admin/login";

        String regex = new PathRegexBuilder()
            .includes(
                "/admin/{id}"
            )
            .excludes(
                "/admin/login"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        assertTrue(router.findRoutes("GET", uri).isEmpty());
    }

    @Test
    public void testExcludeAnt() {
        String uri = "/admin/login";

        // ...why
        String regex = new PathRegexBuilder()
            .includes(
                "/admin/login"
            )
            .excludes(
                "/admin/{id}"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        assertTrue(router.findRoutes("GET", uri).isEmpty());
    }

    @Test
    public void testAntPathParameter() throws Exception {
        String uri = "/aaa";

        String regex = new PathRegexBuilder()
            .includes(
                "/{id}"
            )
            .excludes(
                "/admin"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        assertEquals("aaa", router.findRoutes("GET", uri).get(0).getPathParameters().get("id"));
    }

    @Test
    public void testMultiPathParameter() throws Exception {
        String uri = "/aaa/posts/bbb";

        String regex = new PathRegexBuilder()
            .includes(
                "/{id}/posts/{pid}"
            )
            .excludes(
                "/admin"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        Map<String, String> parameterMap = router.findRoutes("GET", uri).get(0).getPathParameters();
        assertEquals("aaa", parameterMap.get("id"));
        assertEquals("bbb", parameterMap.get("pid"));
    }

    @Test
    public void testMultiIncludeAntWithSameRegex() throws Exception {
        String uri = "/aaa";

        String regex = new PathRegexBuilder()
            .includes(
                "/{id}",
                "/{name}"
            )
            .excludes(
                "/admin"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        Map<String, String> parameterMap = router.findRoutes("GET", uri).get(0).getPathParameters();
        assertEquals(2, parameterMap.size());
        assertEquals("aaa", parameterMap.get("id"));
        assertEquals(null, parameterMap.get("name")); // map had name key, but value is null
    }

    @Test
    public void testMultiIncludeAntWithCustomRegex() throws Exception {
        String uri = "/aaa";
        String numUri = "/123";

        String regex = new PathRegexBuilder()
            .includes(
                "/{id: [0-9]+}",
                "/{name: \\w+}"
            )
            .excludes(
                "/admin"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        Map<String, String> parameterMap = router.findRoutes("GET", numUri).get(0).getPathParameters();
        assertEquals("123", parameterMap.get("id"));
        parameterMap = router.findRoutes("GET", uri).get(0).getPathParameters();
        assertEquals("aaa", parameterMap.get("name"));
    }

    @Test
    public void testUnderlineInPathParameter() throws Exception {
        String uri = "/aaa";

        String regex = new PathRegexBuilder()
            .includes(
                "/{user_id}"
            )
            .excludes(
                "/admin"
            )
            .build();

        router.addRoute(Route.GET(regex, emptyHandler));
        Map<String, String> parameterMap = router.findRoutes("GET", uri).get(0).getPathParameters();
        assertEquals("aaa", parameterMap.get("user_id"));
    }

}
