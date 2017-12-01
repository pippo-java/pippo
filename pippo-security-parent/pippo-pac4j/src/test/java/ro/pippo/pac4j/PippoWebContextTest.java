/*
 * Copyright (C) 2017 the original author or authors.
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
package ro.pippo.pac4j;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.session.SessionStore;
import ro.pippo.core.Application;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.Session;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.Router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Ranganath Kini
 */
@RunWith(MockitoJUnitRunner.class)
public class PippoWebContextTest {

    private static final String DEFAULT_APPLICATION_PATH = "/mock";
    private static final Enumeration<String> EMPTY_ENUMERATION = Collections.emptyEnumeration();

    @Mock
    private HttpServletRequest mockHttpRequest;

    @Mock
    private HttpServletResponse mockHttpResponse;

    @Mock
    private Session mockSession;

    @Mock
    private RouteContext mockRouteContext;

    @Mock
    private Router mockRouter;

    @Mock
    private Application mockApplication;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Response response;

    @Before
    public void setup() {
        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockRouter.getApplicationPath()).thenReturn(DEFAULT_APPLICATION_PATH);
        when(mockApplication.getRouter()).thenReturn(mockRouter);
        when(mockHttpRequest.getParameterNames()).thenReturn(EMPTY_ENUMERATION);
    }

    @Test
    public void shouldReturnPippoSessionStoreWhenAccessed() {
        PippoWebContext context = makePippoWebContext();

        SessionStore<PippoWebContext> sessionStore = context.getSessionStore();
        assertThat(sessionStore, instanceOf(PippoSessionStore.class));
        assertThat(sessionStore.getTrackableSession(context), is(mockSession));
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptionIfSettingSessionStoreIsAttempted() {
        PippoWebContext context = makePippoWebContext();

        thrown.expect(UnsupportedOperationException.class);

        context.setSessionStore(new PippoSessionStore());
    }

    @Test
    public void shouldGetRequestParameterWithSpecifiedName() {
        String expectedParameterName = "foo";
        String expectedParameterValue = "bar";

        when(mockHttpRequest.getParameterNames()).thenReturn(
            makeParameterNamesEnumeration(expectedParameterName));
        when(mockHttpRequest.getParameterValues(anyString())).thenReturn(
            new String[] {expectedParameterValue});

        PippoWebContext context = makePippoWebContext();

        assertThat(context.getRequestParameter(expectedParameterName),
            is(expectedParameterValue));

        verify(mockHttpRequest, times(1)).getParameterNames();
        verify(mockHttpRequest, times(1)).getParameterValues(expectedParameterName);
    }

    @Test
    public void shouldReturnNullValueIfNoRequestParameterWithSpecifiedNameWasFound() {
        String expectedParameterName = "foo";

        when(mockHttpRequest.getParameterNames()).thenReturn(
            makeParameterNamesEnumeration(expectedParameterName));
        when(mockHttpRequest.getParameterValues(anyString())).thenReturn(null);

        PippoWebContext context = makePippoWebContext();

        assertThat(context.getRequestParameter(expectedParameterName),
            is(nullValue()));

        verify(mockHttpRequest, times(1)).getParameterNames();
        verify(mockHttpRequest, times(1)).getParameterValues(expectedParameterName);
    }

    @Test
    public void shouldReturnRequestParametersWithParametersFromUnderlyingRequest() {
        String fooParamName = "fooKEY";
        String barParamName = "barKEY";

        when(mockHttpRequest.getParameterNames()).thenReturn(makeParameterNamesEnumeration(fooParamName, barParamName));
        when(mockHttpRequest.getParameterValues(anyString()))
            .thenReturn(new String[] { "fooValue" })
            .thenReturn(new String[] { "barValue" });

        PippoWebContext context = makePippoWebContext();

        Map<String, String[]> parameterMap = context.getRequestParameters();

        assertThat(parameterMap.get(fooParamName), is(new String[] { "fooValue" }));
        assertThat(parameterMap.get(barParamName), is(new String[] { "barValue" }));

        verify(mockHttpRequest, times(1)).getParameterNames();
        verify(mockHttpRequest, times(1)).getParameterValues(fooParamName);
        verify(mockHttpRequest, times(1)).getParameterValues(barParamName);
    }

    @Test
    public void shouldGetRequestAttributeFromUnderlyingHttpServletRequestWithSpecifiedName() {
        String expectedAttributeName = "foo";
        String expectedAttributeValue = "bar";

        when(mockHttpRequest.getAttribute(anyString())).thenReturn(expectedAttributeValue);

        PippoWebContext context = makePippoWebContext();

        assertThat(context.getRequestAttribute(expectedAttributeName), is(expectedAttributeValue));

        verify(mockHttpRequest, times(1)).getAttribute(expectedAttributeName);
    }

    @Test
    public void shouldReturnNullIfNoRequestAttributeFoundInUnderlyingHttpServletRequestWithSpecifiedName() {
        String expectedAttributeName = "foo";

        when(mockHttpRequest.getAttribute(anyString())).thenReturn(null);

        PippoWebContext context = makePippoWebContext();

        assertThat(context.getRequestAttribute(expectedAttributeName), is(nullValue()));

        verify(mockHttpRequest, times(1)).getAttribute(expectedAttributeName);
    }

    @Test
    public void shouldSetAttributeOnUnderlyingRequestWithSpecifiedNameAndValue() {
        String expectedAttributeName = "foo";
        String expectedAttributeValue = "fooAttrValue";

        when(mockHttpRequest.getAttribute(anyString())).thenReturn(expectedAttributeValue);

        PippoWebContext context = makePippoWebContext();

        context.setRequestAttribute(expectedAttributeName, expectedAttributeValue);

        assertThat(context.getRequestAttribute(expectedAttributeName), is(expectedAttributeValue));

        verify(mockHttpRequest, times(1)).setAttribute(expectedAttributeName, expectedAttributeValue);
        verify(mockHttpRequest, times(1)).getAttribute(expectedAttributeName);
    }

    @Test
    public void shouldGetServerNameFromUnderlyingRequest() {
        String expectedServerName = "localhost";

        when(mockHttpRequest.getServerName()).thenReturn(expectedServerName);

        PippoWebContext context = makePippoWebContext();

        assertThat(context.getServerName(), is(expectedServerName));

        verify(mockHttpRequest, times(1)).getServerName();
    }

    @Test
    public void shouldGetServerPortFromUnderlyingRequest() {
        int expectedServerPort = 8080;

        when(mockHttpRequest.getServerPort()).thenReturn(expectedServerPort);

        PippoWebContext context = makePippoWebContext();

        assertThat(context.getServerPort(), is(expectedServerPort));

        verify(mockHttpRequest, times(1)).getServerPort();
    }

    @Test
    public void shouldGetSchemeFromUnderlyingRequest() {
        String expectedScheme = "https";

        when(mockHttpRequest.getScheme()).thenReturn(expectedScheme);

        PippoWebContext context = makePippoWebContext();

        assertThat(context.getScheme(), is(expectedScheme));

        verify(mockHttpRequest, times(1)).getScheme();
    }

    @Test
    public void shouldReturnWhetherOrNotRequestIsSecureFromUnderlyingRequest() {
        boolean expectedSecureStatus = true;

        when(mockHttpRequest.isSecure()).thenReturn(expectedSecureStatus);

        PippoWebContext context = makePippoWebContext();

        assertThat(context.isSecure(), is(expectedSecureStatus));

        verify(mockHttpRequest, times(1)).isSecure();
    }

    @Test
    public void shouldReturnUrlFromUnderlyingRequestAsFullRequestUrl() {
        String expectedUrl = "https://foo.example.com/2324/lists";

        when(mockHttpRequest.getRequestURL()).thenReturn(new StringBuffer(expectedUrl));

        PippoWebContext context = makePippoWebContext();

        assertThat(context.getFullRequestURL(), is(expectedUrl));

        verify(mockHttpRequest, times(1)).getRequestURL();
    }

    @Test
    public void shouldReturnPathFromUnderlyingRequest() {
        String expectedPath = "/2324/lists/43/foo";
        StringBuffer expectedUrl = new StringBuffer("https://foo.example.com")
            .append(DEFAULT_APPLICATION_PATH)
            .append(expectedPath);

        when(mockRouter.getApplicationPath()).thenReturn(DEFAULT_APPLICATION_PATH);
        when(mockHttpRequest.getRequestURL()).thenReturn(expectedUrl);

        PippoWebContext context = makePippoWebContext();

        assertThat(context.getPath(), is(expectedPath));

        verify(mockRouter, atLeastOnce()).getApplicationPath();
        verify(mockHttpRequest, times(1)).getRequestURL();
    }

    @Test
    public void shouldReturnRouteContext() {
        PippoWebContext context = makePippoWebContext();

        assertThat(context.getRouteContext(), is(mockRouteContext));
    }

    @Test
    public void shouldReturnAllCookiesFromUnderlyingRequest() {
        int authCookieExpiry = 604800000;

        javax.servlet.http.Cookie testCookie = new javax.servlet.http.Cookie("test", "talk");
        javax.servlet.http.Cookie authCookie = new javax.servlet.http.Cookie("sid", "DEADBEEF");
        authCookie.setDomain("foo.example.com");
        authCookie.setPath(DEFAULT_APPLICATION_PATH);
        authCookie.setComment("auth cookie");
        authCookie.setHttpOnly(true);
        authCookie.setSecure(true);
        authCookie.setMaxAge(authCookieExpiry);

        when(mockHttpRequest.getCookies())
            .thenReturn(new javax.servlet.http.Cookie[] { testCookie, authCookie });

        PippoWebContext context = makePippoWebContext();

        Collection<Cookie> cookies = context.getRequestCookies();

        assertThat(cookies.size(), is(2));
        assertTrue(cookies.stream().anyMatch(cookie ->
            cookie.getName().equals("test") &&
                cookie.getValue().equals("talk")));

        assertTrue(cookies.stream().anyMatch(cookie ->
            cookie.getName().equals("sid") &&
                cookie.getValue().equals("DEADBEEF") &&
                cookie.getPath().equals(DEFAULT_APPLICATION_PATH) &&
                cookie.getDomain().equals("foo.example.com") &&
                cookie.getComment().equals("auth cookie") &&
                cookie.isSecure() &&
                cookie.getMaxAge() == authCookieExpiry &&
                cookie.isHttpOnly()));

        verify(mockHttpRequest, times(1)).getCookies();
    }

    @Test
    public void shouldReturnEmptyCollectionIfNoCookiesInUnderlyingRequest() {
        when(mockHttpRequest.getCookies())
            .thenReturn(new javax.servlet.http.Cookie[0]);

        PippoWebContext context = makePippoWebContext();

        Collection<Cookie> cookies = context.getRequestCookies();

        assertTrue(cookies.isEmpty());

        verify(mockHttpRequest, times(1)).getCookies();
    }

    @Test
    public void shouldAddSpecifiedCookieToUnderlyingResponse() {
        Cookie cookieToAdd = new Cookie("auth", "DEADBEEF");
        cookieToAdd.setMaxAge(2342);
        cookieToAdd.setSecure(true);
        cookieToAdd.setHttpOnly(true);
        cookieToAdd.setDomain("foo.example.com");
        cookieToAdd.setPath(DEFAULT_APPLICATION_PATH);
        cookieToAdd.setComment("auth cookie");

        makePippoWebContext().addResponseCookie(cookieToAdd);

        ArgumentCaptor<javax.servlet.http.Cookie> cookieArgumentCaptor
            = ArgumentCaptor.forClass(javax.servlet.http.Cookie.class);

        response.commit();

        verify(mockHttpResponse, times(1)).addCookie(cookieArgumentCaptor.capture());

        javax.servlet.http.Cookie cookieAdded = cookieArgumentCaptor.getValue();

        assertThat(cookieAdded.getName(), is(cookieToAdd.getName()));
        assertThat(cookieAdded.getValue(), is(cookieToAdd.getValue()));
        assertThat(cookieAdded.getMaxAge(), is(cookieToAdd.getMaxAge()));
        assertThat(cookieAdded.getSecure(), is(cookieToAdd.isSecure()));
        assertThat(cookieAdded.isHttpOnly(), is(cookieToAdd.isHttpOnly()));
        assertThat(cookieAdded.getComment(), is(cookieToAdd.getComment()));
        assertThat(cookieAdded.getDomain(), is(cookieToAdd.getDomain()));
        assertThat(cookieAdded.getPath(), is(cookieToAdd.getPath()));
    }

    private PippoWebContext makePippoWebContext() {
        Request request = makeRequest();
        response = makeResponse();

        when(mockRouteContext.getRequest()).thenReturn(request);
        when(mockRouteContext.getResponse()).thenReturn(response);

        return new PippoWebContext(mockRouteContext, null);
    }

    private Request makeRequest() {
        return new Request(mockHttpRequest, mockApplication);
    }

    private Response makeResponse() {
        return new Response(mockHttpResponse, mockApplication);
    }

    private Enumeration<String> makeParameterNamesEnumeration(String... paramNames) {
        return Collections.enumeration(Arrays.asList(paramNames));
    }

}
