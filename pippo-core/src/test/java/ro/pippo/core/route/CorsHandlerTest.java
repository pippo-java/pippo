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
package ro.pippo.core.route;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_ALLOW_ORIGIN;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_EXPOSE_HEADERS;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_MAX_AGE;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_ALLOW_METHODS;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_ALLOW_HEADERS;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ro.pippo.core.Application;
import ro.pippo.core.Request;
import ro.pippo.core.Response;

/**
 * @author Dwouglas Mhagnum
 *
 *         Se tiver o erro
 *         {@literal java.lang.IllegalStateException: Could not initialize plugin: interface org.mockito.plugins.MockMaker (alternate: null)}
 *         executando esse teste, verifique se não o está rodando com a JRE ao
 *         invés da JDK.
 */
// @RunWith(MockitoJUnitRunner.class) // TODO: precisa ?
public class CorsHandlerTest {

    // private Application application;
    private RouteContext routeContext;
    // private CorsHandler corsHandler;
    private Response response;

    @Before
    public void setUp() {
        // Application application = Mockito.mock(Application.class);
        Application application = new Application();

//         Request request = Mockito.mock(Request.class);
//        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
//        Request request = new Request(servletRequest, application);

//         Response response = Mockito.mock(Response.class);
//        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
//        Response response = new Response(servletResponse, application);

        // routeContext = new DefaultRouteContext(application, request, response,
        // Collections.emptyList());
//        routeContext = new DefaultRouteContext(application, request, response, Collections.emptyList());
//         routeContext = Mockito.mock(DefaultRouteContext.class);
        routeContext = Mockito.mock(RouteContext.class);

        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        response = Mockito.spy(new Response(servletResponse, application));
        doReturn(response).when(routeContext).getResponse();
    }

    @Test
    public void test_OnlyHeaderAllowOrigin() {
        // arrange
        String originExpected = "http://fake.address.com";
        // String exposeHeadersExpected = "fake1,fake2,fake3";

        CorsHandler corsHandler = new CorsHandler(originExpected);
        // corsHandler.exposeHeaders(exposeHeadersExpected);

        doReturn("GET").when(routeContext).getRequestMethod();

        // act
        corsHandler.handle(routeContext);

        // assert
        assertEquals(originExpected, response.getHeader(ACCESS_CONTROL_ALLOW_ORIGIN));
        // assertEquals(exposeHeadersExpected, response.getHeader(ACCESS_CONTROL_EXPOSE_HEADERS));
        Mockito.verify(routeContext, Mockito.times(1)).next();
        Mockito.verify(response, Mockito.never()).accepted();
    }

    @Test
    public void test_AllHeaders() {
        // arrange
        String originExpected = "http://fake.address.com";
        String exposeHeadersExpected = "fake1,fake2,fake3";
        String allowCredentialsExpected = "true";
        String maxAgeExpected = "3600";

        CorsHandler corsHandler = new CorsHandler(originExpected);
        corsHandler.exposeHeaders(exposeHeadersExpected);
        corsHandler.allowCredentials(Boolean.valueOf(allowCredentialsExpected));
        corsHandler.maxAge(Integer.valueOf(maxAgeExpected));

        doReturn("GET").when(routeContext).getRequestMethod();

        // act
        corsHandler.handle(routeContext);

        // assert
        assertEquals(originExpected, response.getHeader(ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals(exposeHeadersExpected, response.getHeader(ACCESS_CONTROL_EXPOSE_HEADERS));
        assertEquals(allowCredentialsExpected, response.getHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(maxAgeExpected, response.getHeader(ACCESS_CONTROL_MAX_AGE));

        Mockito.verify(routeContext, Mockito.times(1)).next();
        Mockito.verify(response, Mockito.never()).accepted();
    }

}
