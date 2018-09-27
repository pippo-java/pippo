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

import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import ro.pippo.core.ParameterValue;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.route.RouteContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pippo specific implementation of {@link WebContext}.
 * Uses specified {@link RouteContext} to provide required details.
 *
 * @author Ranganath Kini
 * @author Decebal Suiu
 */
public class PippoWebContext implements WebContext {

    private final RouteContext routeContext;
    private final SessionStore<PippoWebContext> sessionStore;

    public PippoWebContext(RouteContext routeContext) {
        this(routeContext, null);
    }

    public PippoWebContext(RouteContext routeContext, SessionStore<PippoWebContext> sessionStore) {
        this.routeContext = routeContext;
        this.sessionStore = (sessionStore != null) ? sessionStore : new PippoSessionStore();
    }

    @Override
    public SessionStore<PippoWebContext> getSessionStore() {
        return sessionStore;
    }

    @Override
    public void setSessionStore(SessionStore sessionStore) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestParameter(String name) {
        ParameterValue parameter = getRequest().getParameter(name);
        return parameter.isNull() ? null : parameter.toString();
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        Map<String, ParameterValue> parameters = getRequest().getParameters();
        final Map<String, String[]> result = new HashMap<>();

        parameters.forEach((s, parameterValue) -> result.put(s, parameterValue.getValues()));

        return result;
    }

    @Override
    public Object getRequestAttribute(String name) {
        return getRequest().getHttpServletRequest().getAttribute(name);
    }

    @Override
    public void setRequestAttribute(String name, Object value) {
        getRequest().getHttpServletRequest().setAttribute(name, value);
    }

    @Override
    public String getRequestHeader(String name) {
        return getRequest().getHeader(name);
    }

    @Override
    public String getRequestMethod() {
        return getRequest().getMethod();
    }

    @Override
    public String getRemoteAddr() {
        return getRequest().getClientIp();
    }

    @Override
    public void writeResponseContent(String contentToWrite) {
        getResponse().getWriter().write(contentToWrite);
    }

    @Override
    public void setResponseStatus(int statusCode) {
        getResponse().status(statusCode);
    }

    @Override
    public void setResponseHeader(String name, String value) {
        getResponse().header(name, value);
    }

    @Override
    public void setResponseContentType(String contentType) {
        getResponse().contentType(contentType);
    }

    @Override
    public String getServerName() {
        return getRequest().getHttpServletRequest().getServerName();
    }

    @Override
    public int getServerPort() {
        return getRequest().getHttpServletRequest().getServerPort();
    }

    @Override
    public String getScheme() {
        return getRequest().getScheme();
    }

    @Override
    public boolean isSecure() {
        return getRequest().isSecure();
    }

    @Override
    public String getFullRequestURL() {
        return getRequest().getUrl();
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        return getRequest().getCookies().stream().map(c -> {
            Cookie cookie = new Cookie(c.getName(), c.getValue());
            cookie.setComment(c.getComment());
            cookie.setSecure(c.getSecure());
            cookie.setPath(c.getPath());
            cookie.setHttpOnly(c.isHttpOnly());
            cookie.setDomain(c.getDomain());
            cookie.setMaxAge(c.getMaxAge());
            cookie.setVersion(c.getVersion());

            return cookie;
        }).collect(Collectors.toList());
    }

    @Override
    public void addResponseCookie(Cookie cookie) {
        getResponse().cookie(cookie.getPath(),
            cookie.getDomain(),
            cookie.getName(),
            cookie.getValue(),
            cookie.getMaxAge(),
            cookie.isSecure()
        );

        javax.servlet.http.Cookie addedCookie = getResponse().getCookie(cookie.getName());
        addedCookie.setHttpOnly(cookie.isHttpOnly());
        addedCookie.setComment(cookie.getComment());
    }

    @Override
    public String getPath() {
        return getRequest().getPath();
    }

    public RouteContext getRouteContext() {
        return routeContext;
    }

    public Request getRequest() {
        return getRouteContext().getRequest();
    }

    public Response getResponse() {
        return getRouteContext().getResponse();
    }

}
