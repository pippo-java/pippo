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
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_ALLOW_HEADERS;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_ALLOW_METHODS;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_ALLOW_ORIGIN;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_EXPOSE_HEADERS;
import static ro.pippo.core.HttpConstants.Header.ACCESS_CONTROL_MAX_AGE;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.pippo.core.Application;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.Response;

/**
 * The following error must be thrown if the test is performed with the JRE
 * instead of the JDK:
 * {@literal java.lang.IllegalStateException: Could not initialize plugin: interface org.mockito.plugins.MockMaker (alternate: null)}
 *
 * @author Dwouglas Mhagnum
 */
public class CorsHandlerTest {

    private RouteContext routeContext;
    private Response response;

    @Before
    public void setUp() {
        Application application = new Application();

        routeContext = Mockito.mock(RouteContext.class);

        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        response = Mockito.spy(new Response(servletResponse, application));
        doReturn(response).when(routeContext).getResponse();
    }

    @Test
    public void test_OnlyHeaderAllowOrigin() {
        // arrange
        String originExpected = "http://fake.address.com";

        CorsHandler corsHandler = new CorsHandler(originExpected);

        doReturn("GET").when(routeContext).getRequestMethod();

        // act
        corsHandler.handle(routeContext);

        // assert
        assertEquals(originExpected, response.getHeader(ACCESS_CONTROL_ALLOW_ORIGIN));
        Mockito.verify(routeContext, Mockito.times(1)).next();
        Mockito.verify(response, Mockito.never()).accepted();
    }

    @Test
    public void test_AllHeaders() {
        // arrange
        String originExpected = "http://fake.address.com";
        String exposeHeadersExpected = "h-fake1,h-fake2,h-fake3";
        String allowCredentialsExpected = "true";
        String maxAgeExpected = "3600";
        String allowMethodsExpected = "put,delete";
        String allowHeadersExpected = "h-fake3,h-fake4";

        CorsHandler corsHandler = new CorsHandler(originExpected);
        corsHandler.exposeHeaders(exposeHeadersExpected);
        corsHandler.allowCredentials(Boolean.valueOf(allowCredentialsExpected));
        corsHandler.maxAge(Integer.valueOf(maxAgeExpected));
        corsHandler.allowMethods(allowMethodsExpected);
        corsHandler.allowHeaders(allowHeadersExpected);

        doReturn("GET").when(routeContext).getRequestMethod();

        // act
        corsHandler.handle(routeContext);

        // assert
        assertEquals(originExpected, response.getHeader(ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals(exposeHeadersExpected, response.getHeader(ACCESS_CONTROL_EXPOSE_HEADERS));
        assertEquals(allowCredentialsExpected, response.getHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(maxAgeExpected, response.getHeader(ACCESS_CONTROL_MAX_AGE));
        assertEquals(allowMethodsExpected, response.getHeader(ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals(allowHeadersExpected, response.getHeader(ACCESS_CONTROL_ALLOW_HEADERS));

        assertEquals(
                "CorsHandler [allowOrigin=http://fake.address.com, allowMethods=put,delete, allowHeaders=h-fake3,h-fake4, exposeHeaders=h-fake1,h-fake2,h-fake3, maxAge=3600, allowCredentials=true]",
                corsHandler.details());

        Mockito.verify(routeContext, Mockito.times(1)).next();
        Mockito.verify(response, Mockito.never()).accepted();
    }

    @Test
    public void test_RequestMethodOptions() {
        // arrange
        String originExpected = "*";

        CorsHandler corsHandler = new CorsHandler(originExpected);

        doReturn("OPTIONS").when(routeContext).getRequestMethod();

        // act
        corsHandler.handle(routeContext);

        // assert
        assertEquals(originExpected, response.getHeader(ACCESS_CONTROL_ALLOW_ORIGIN));
        Mockito.verify(routeContext, Mockito.never()).next();
        Mockito.verify(response, Mockito.times(1)).accepted();
    }

    @Test
    public void test_InvalidAllowOrigin() {
        final String expected = "allowOrigin cannot be blank";

        {
            PippoRuntimeException exception = assertThrows(PippoRuntimeException.class, () -> {
                new CorsHandler("    ");
            });

            assertEquals(expected, exception.getMessage());
        }

        {
            PippoRuntimeException exception = assertThrows(PippoRuntimeException.class, () -> {
                new CorsHandler(null);
            });

            assertEquals(expected, exception.getMessage());
        }
    }

}
