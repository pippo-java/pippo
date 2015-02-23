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
package ro.pippo.core.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.ParameterValue;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.Session;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author James Moger
 */
public class DefaultRouteContext implements RouteContext {

    private static final Logger log = LoggerFactory.getLogger(DefaultRouteContext.class);

    protected final Application application;
    protected final Request request;
    protected final Response response;
    protected final Iterator<RouteMatch> iterator;

    protected DefaultRouteContext(Application application, Request request, Response response, List<RouteMatch> routeMatches) {
        this.application = application;
        this.request = request;
        this.response = response;
        this.iterator = routeMatches.iterator();
    }

    @Override
    public Application getApplication() {
        return application;
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public Response getResponse() {
        return response;
    }

    @Override
    public boolean hasSession() {
        return request.getSession(false) != null;
    }

    @Override
    public Session getSession() {
        return request.getSession();
    }

    @Override
    public void recreateSession() {
        request.recreateSession();
    }

    @Override
    public void resetSession() {
        request.resetSession();
    }

    @Override
    public void touchSession() {
        request.getSession().touch();
    }

    @Override
    public void invalidateSession() {
        request.getSession().invalidate();
    }

    @Override
    public <T> T putSession(String name, T t) {
        getSession().put(name, t);
        return t;
    }

    @Override
    public <T> T fromSession(String name) {
        if (hasSession()) {
            T t = request.getSession().get(name);
            return t;
        }
        return null;
    }

    @Override
    public <T> T removeSession(String name) {
        if (hasSession()) {
            T t = request.getSession().remove(name);
            return t;
        }
        return null;
    }

    @Override
    public <T> T putLocal(String name, T t) {
        response.getLocals().put(name, t);
        return t;
    }

    @Override
    public void putLocals(Map<String, Object> locals) {
        response.getLocals().putAll(locals);
    }

    @Override
    public <T> T fromLocal(String name) {
        T t = (T) response.getLocals().get(name);
        return t;
    }

    @Override
    public <T> T removeLocal(String name) {
        T t = (T) response.getLocals().remove(name);
        return t;
    }

    @Override
    public ParameterValue fromRequest(String name) {
        ParameterValue parameterValue = request.getParameter(name);
        return parameterValue;
    }

    @Override
    public ParameterValue fromHeader(String name) {
        ParameterValue parameterValue = request.getParameter(name);
        return parameterValue;
    }

    @Override
    public <T> T putHeader(String name, T t) {
        response.header(name, t.toString());
        return t;
    }

    @Override
    public void flashError(String message, Object... args) {
        request.getSession().getFlash().error(message, args);
    }

    @Override
    public void flashWarning(String message, Object... args) {
        request.getSession().getFlash().warning(message, args);
    }

    @Override
    public void flashInfo(String message, Object... args) {
        request.getSession().getFlash().info(message, args);
    }

    @Override
    public void flashSuccess(String message, Object... args) {
        request.getSession().getFlash().success(message, args);
    }

    @Override
    public <T> T createEntityFromParameters(Class<T> classOfT) {
        return request.createEntityFromParameters(classOfT);
    }

    @Override
    public <T> T createEntityFromBody(Class<T> classOfT) {
        return request.createEntityFromBody(classOfT);
    }

    @Override
    public String getRequestUri() {
        return request.getContextUri();
    }

    @Override
    public String getRequestMethod() {
        return request.getMethod();
    }

    @Override
    public boolean isRequestMethod(String method) {
        return request.getMethod().equalsIgnoreCase(method);
    }

    @Override
    public void render(String templateName) {
        response.render(templateName);
    }

    @Override
    public void send(CharSequence content) {
        response.send(content);
    }

    @Override
    public void send(File file) {
        response.send(file);
    }

    @Override
    public void send(Object object) {
        response.send(object);
    }

    @Override
    public void redirect(String path) {
        response.redirectToContextPath(path);
    }

    @Override
    public void next() {
        if (iterator.hasNext()) {
            // retrieves the next route
            RouteMatch routeMatch = iterator.next();
            Route route = routeMatch.getRoute();
            log.debug("Found {}", route);

            // set the new path parameters in request
            Map<String, String> pathParameters = routeMatch.getPathParameters();
            if (pathParameters != null) {
                request.setPathParameters(pathParameters);
                log.debug("Added path parameters to request");
            }

            // remove route from chain
            iterator.remove();

            log.debug("Call handler for {}", route);
            handleRoute(route);
        }
    }

    /**
     * Execute all routes that are flagged to run as finally.
     */
    @Override
    public void runFinallyRoutes() {
        while (iterator.hasNext()) {
            RouteMatch routeMatch = iterator.next();
            if (routeMatch.getRoute().isRunAsFinally()) {
                try {
                    handleRoute(routeMatch.getRoute());
                } catch (Exception e) {
                    log.error("Unexpected error in Finally Route", e);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("chain.next() not called, skipping {}", routeMatch);
            }
        }
    }

    @Override
    public RouteContext text() {
        response.text();

        return this;
    }

    @Override
    public RouteContext xml() {
        response.xml();

        return this;
    }

    @Override
    public RouteContext json() {
        response.json();

        return this;
    }

    @Override
    public RouteContext yaml() {
        response.yaml();

        return this;
    }

    @Override
    public RouteContext html() {
        response.html();

        return this;
    }

    @Override
    public RouteContext negotiateContentType() {
        response.contentType(request);

        return this;
    }

    @Override
    public RouteContext status(int code) {
        response.status(code);

        return this;
    }


    protected void handleRoute(Route route) {
        route.getRouteHandler().handle(this);
    }
}
