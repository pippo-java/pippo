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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Decebal Suiu
 * @author James Moger
 */
public class DefaultErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultErrorHandler.class);

    private Application application;

    public DefaultErrorHandler(Application application) {
        this.application = application;
    }

    @Override
    public void handle(int statusCode, RouteContext routeContext) {
        routeContext.status(statusCode);

        // start with the content type specified for the response
        String contentType = routeContext.getResponse().getContentType();

        if (StringUtils.isNullOrEmpty(contentType)) {
            if (!StringUtils.isNullOrEmpty(routeContext.getRequest().getAcceptType())) {
                String acceptType = routeContext.getRequest().getAcceptType();
                if (acceptType.startsWith(HttpConstants.ContentType.TEXT_HTML)
                    || acceptType.startsWith(HttpConstants.ContentType.TEXT_XHTML)) {

                    // exception during a browser request
                    contentType = HttpConstants.ContentType.TEXT_HTML;
                }
            }

            if (StringUtils.isNullOrEmpty(contentType)) {
                // unspecified so negotiate a content type based on the request
                routeContext.negotiateContentType();

                // retrieve the negotiated type
                contentType = routeContext.getResponse().getContentType();
            }
        }

        if (StringUtils.isNullOrEmpty(contentType)) {
            log.debug("No accept type nor content type specified! Defaulting to text/html.");
            renderHtml(statusCode, routeContext);
        } else if (contentType.equals(HttpConstants.ContentType.TEXT_HTML)) {
            // render an html page
            renderHtml(statusCode, routeContext);
        } else {
            // render an object representation
            ContentTypeEngine engine = application.getContentTypeEngine(contentType);
            if (engine == null) {
                log.warn("No registered content type engine for '{}'", contentType);
                renderHtml(statusCode, routeContext);
            } else {
                Error error = prepareError(statusCode, routeContext);
                try {
                    routeContext.getResponse().contentType(engine.getContentType()).send(error);
                } catch (Exception e) {
                    log.error("Unexpected error generating '{}' as '{}'!", Error.class.getName(), contentType, e);
                    routeContext.status(HttpConstants.StatusCode.INTERNAL_ERROR);
                    routeContext.send(application.getMessages().get("pippo.statusCode500", routeContext));
                }
            }
        }
    }

    protected void renderHtml(int statusCode, RouteContext routeContext) {
        if (application.getTemplateEngine() == null) {
            renderDirectly(routeContext);
        } else {
            String template = getTemplateForStatusCode(statusCode);
            if (template == null) {
                log.debug("There is no {} template for status code '{}'", application.getTemplateEngine().getClass()
                    .getSimpleName(), statusCode);
                renderDirectly(routeContext);
            } else {
                try {
                    Error error = prepareError(statusCode, routeContext);
                    Map<String, Object> bindings = error.asMap();
                    bindings.putAll(prepareTemplateBindings(statusCode, routeContext));
                    routeContext.putLocals(bindings);
                    routeContext.render(template);
                } catch (Exception e) {
                    log.error("Unexpected error rendering your '{}' template!", template, e);
                    handle(e, routeContext);
                }
            }
        }
    }

    @Override
    public void handle(Exception exception, RouteContext routeContext) {
        if (routeContext.getResponse().isCommitted()) {
            log.debug("The response has already been committed. Cannot use the exception handler.");
            return;
        }

        String message = exception.getMessage();
        if (!StringUtils.isNullOrEmpty(message) && !routeContext.getResponse().getLocals().containsKey("message")) {
            routeContext.putLocal("message", message);
        }

        if (application.getPippoSettings().isDev()) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            exception.printStackTrace(printWriter);
            String stackTrace = stringWriter.toString();
            routeContext.putLocal("stacktrace", stackTrace);
        }

        handle(HttpConstants.StatusCode.INTERNAL_ERROR, routeContext);
    }

    /**
     * Render the result directly.
     *
     * @param routeContext
     */
    protected void renderDirectly(RouteContext routeContext) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<div>");
        content.append("Cannot find a route for '");
        content.append(routeContext.getRequestMethod());
        content.append(" ");
        content.append(routeContext.getRequestUri());
        content.append("'</div>");
        content.append("<div>Available routes:</div>");
        content.append("<ul style=\" list-style-type: none; margin: 0; \">");
        List<Route> routes = application.getRouter().getRoutes();
        for (Route route : routes) {
            content.append("<li>");
            content.append(route.getRequestMethod());
            content.append(" ");
            content.append(route.getUriPattern());
            content.append("</li>");
        }
        content.append("</ul>");
        content.append("</body></html>");

        routeContext.send(content);
    }

    /**
     * Get the template bindings for the error response.
     *
     * @param routeContext
     * @return bindings map
     */
    protected Map<String, Object> prepareTemplateBindings(int statusCode, RouteContext routeContext) {
        Map<String, Object> locals = new LinkedHashMap<>();
        locals.put("applicationName", application.getApplicationName());
        locals.put("applicationVersion", application.getApplicationVersion());
        locals.put("runtimeMode", application.getPippoSettings().getRuntimeMode().toString());
        if (application.getPippoSettings().isDev()) {
            locals.put("routes", application.getRouter().getRoutes());
        }

        return locals;
    }

    /**
     * Prepares an Error instance for the error response.
     *
     * @param statusCode
     * @param routeContext
     * @return an Error
     */
    protected Error prepareError(int statusCode, RouteContext routeContext) {
        String messageKey = "pippo.statusCode" + statusCode;

        Error error = new Error();
        error.statusCode = statusCode;
        error.statusMessage = application.getMessages().get(messageKey, routeContext);
        error.requestMethod = routeContext.getRequestMethod();
        error.requestUri = routeContext.getRequestUri();
        error.requestUri = routeContext.getRequestUri();
        error.stacktrace = routeContext.fromLocal("stacktrace");
        error.message = routeContext.fromLocal("message");

        return error;
    }

    protected String getTemplateForStatusCode(int statusCode) {
        switch (statusCode) {
            case HttpConstants.StatusCode.BAD_REQUEST:
                return TemplateEngine.BAD_REQUEST_400;
            case HttpConstants.StatusCode.UNAUTHORIZED:
                return TemplateEngine.UNAUTHORIZED_401;
            case HttpConstants.StatusCode.PAYMENT_REQUIRED:
                return TemplateEngine.PAYMENT_REQUIRED_402;
            case HttpConstants.StatusCode.FORBIDDEN:
                return TemplateEngine.FORBIDDEN_403;
            case HttpConstants.StatusCode.NOT_FOUND:
                return TemplateEngine.NOT_FOUND_404;
            case HttpConstants.StatusCode.METHOD_NOT_ALLOWED:
                return TemplateEngine.METHOD_NOT_ALLOWED_405;
            case HttpConstants.StatusCode.CONFLICT:
                return TemplateEngine.CONFLICT_409;
            case HttpConstants.StatusCode.GONE:
                return TemplateEngine.GONE_410;
            default:
            case HttpConstants.StatusCode.INTERNAL_ERROR:
                return TemplateEngine.INTERNAL_ERROR_500;
            case HttpConstants.StatusCode.NOT_IMPLEMENTED:
                return TemplateEngine.NOT_IMPLEMENTED_501;
            case HttpConstants.StatusCode.OVERLOADED:
                return TemplateEngine.OVERLOADED_502;
            case HttpConstants.StatusCode.SERVICE_UNAVAILABLE:
                return TemplateEngine.SERVICE_UNAVAILABLE_503;
        }
    }

}
