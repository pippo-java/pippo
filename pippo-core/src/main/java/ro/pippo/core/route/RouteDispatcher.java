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
import ro.pippo.core.ErrorHandler;
import ro.pippo.core.Flash;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.util.ServiceLocator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * RouteDispatcher is the common core of Pippo route dispatching.
 * This core is shared by PippoFilter and PippoServlet or may be
 * used in your own servlet or filter.
 *
 * @author Decebal Suiu
 * @author James Moger
 */
public class RouteDispatcher {

    private final static Logger log = LoggerFactory.getLogger(RouteDispatcher.class);

    private final static ThreadLocal<RouteContext> ROUTE_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    private static final List<RouteMatch> noMatches = Collections.emptyList();

    private RouteContextFactory<?> routeContextFactory;
    private Application application;
    private Router router;
    private ErrorHandler errorHandler;
    private RouteHandler notFoundRouteHandler;

    @SuppressWarnings("unchecked")
    public static <T extends RouteContext> T getRouteContext() {
        return (T) ROUTE_CONTEXT_THREAD_LOCAL.get();
    }

    public RouteDispatcher(Application application) {
        this.application = application;
    }

    public Application getApplication() {
        return application;
    }

    public void init() {
        log.debug("Initializing application '{}'", application);
        application.init();

        router = application.getRouter();
        errorHandler = application.getErrorHandler();
        notFoundRouteHandler = application.getNotFoundRouteHandler();

        routeContextFactory = getRouteContextFactory();
        routeContextFactory.init(application);
        log.debug("RouteContext factory is '{}'", routeContextFactory.getClass().getName());
    }

    protected RouteContextFactory<?> getRouteContextFactory() {
        RouteContextFactory<?> factory = ServiceLocator.locate(RouteContextFactory.class);
        if (factory == null) {
            factory = new DefaultRouteContextFactory();
        }

        return factory;
    }

    /**
     * Dispatches the Request/Response.
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void dispatch(Request request, Response response) throws IOException, ServletException {
        onPreDispatch(request, response);
        onRouteDispatch(request, response);
        onPostDispatch(request, response);
    }

    /**
     * Executes onPreDispatch of registered route pre-dispatch listeners.
     *
     * @param request
     * @param response
     */
    protected void onPreDispatch(Request request, Response response) {
        application.getRoutePreDispatchListeners().onPreDispatch(request, response);
    }

    /**
     * onRouteDispatch is the front-line for Route processing.
     *
     * @param request
     * @param response
     */
    protected void onRouteDispatch(Request request, Response response) {
        final String requestPath = request.getPath();
        final String requestMethod = request.getMethod();

        if (shouldIgnorePath(requestPath)) {
            // NOT FOUND (404)
            RouteContext routeContext = routeContextFactory.createRouteContext(application, request, response, noMatches);
            ROUTE_CONTEXT_THREAD_LOCAL.set(routeContext);
            errorHandler.handle(HttpServletResponse.SC_NOT_FOUND, routeContext);
            ROUTE_CONTEXT_THREAD_LOCAL.remove();
            log.debug("Returned status code {} for {} '{}' (IGNORED)", response.getStatus(), requestMethod, requestPath);

            return;
        }

        List<RouteMatch> routeMatches = router.findRoutes(requestMethod, requestPath);
        RouteContext routeContext = routeContextFactory.createRouteContext(application, request, response, routeMatches);
        ROUTE_CONTEXT_THREAD_LOCAL.set(routeContext);

        try {
            if (routeMatches.isEmpty()) {
                if (notFoundRouteHandler != null) {
                    notFoundRouteHandler.handle(routeContext);
                } else {
                    // NOT FOUND (404)
                    errorHandler.handle(HttpConstants.StatusCode.NOT_FOUND, routeContext);
                }
            } else {
                processFlash(routeContext);
            }

            // execute the chain
            routeContext.next();

            if (!response.isCommitted()) {
                if (response.getStatus() == 0) {
                    log.debug("Status code not set for {} '{}'", requestMethod, requestPath);
                    response.notFound();
                }
                log.debug("Auto-committing response for {} '{}'", requestMethod, requestPath);
                if (response.getStatus() >= HttpServletResponse.SC_BAD_REQUEST) {
                    // delegate response to the error handler.
                    // this will generate response content appropriate for the request
                    errorHandler.handle(response.getStatus(), routeContext);
                } else {
                    response.commit();
                }
            }
        } catch (Exception e) {
            errorHandler.handle(e, routeContext);
        } finally {
            routeContext.runFinallyRoutes();
            log.debug("Returned status code {} for {} '{}'", response.getStatus(), requestMethod, requestPath);
            ROUTE_CONTEXT_THREAD_LOCAL.remove();
        }
    }

    /**
     * Executes onPostDispatch of registered route post-dispatch listeners.
     *
     * @param request
     * @param response
     */
    protected void onPostDispatch(Request request, Response response) {
        application.getRoutePostDispatchListeners().onPostDispatch(request, response);
    }

    /**
     * Returns true if the request should be ignored.
     * If ignored, the response code is set to NOT FOUND (404).
     *
     * @param requestUri
     * @return true if the request should be ignored
     */
    protected boolean shouldIgnorePath(String requestUri) {
        for (String path : router.getIgnorePaths()) {
            if (requestUri.startsWith(path)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes a Flash instance from the session, binds it to the RouteContext,
     * and creates a new Flash instance.
     *
     * @param routeContext
     */
    private void processFlash(RouteContext routeContext) {
        Flash flash = null;

        if (routeContext.hasSession()) {
            // get flash from session
            flash = routeContext.removeSession("flash");
            // put an empty flash (outgoing flash) in session; defense against session.get("flash")
            routeContext.setSession("flash", new Flash());
        }

        if (flash == null) {
            flash = new Flash();
        }

        // make current flash available to templates
        routeContext.setLocal("flash", flash);
    }

}
