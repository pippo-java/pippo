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

import ro.fortsoft.pippo.core.route.Route;
import ro.fortsoft.pippo.core.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void handle(int statusCode, Request request, Response response) {
        response.status(statusCode);

        // start with the content type specified for the response
        String contentType = response.getContentType();

        if (StringUtils.isNullOrEmpty(contentType)) {
            // unspecified so negotiate a content type based on the request
            response.contentType(request);

            // retrieve the negotiated type
            contentType = response.getContentType();
        }

        if (StringUtils.isNullOrEmpty(contentType)) {

            log.warn("No content type specified!'");
            renderHtml(statusCode, request, response);

        } else if (contentType.startsWith(HttpConstants.ContentType.TEXT_HTML)
                || contentType.startsWith(HttpConstants.ContentType.TEXT_XHTML)) {

            // render an html page
            renderHtml(statusCode, request, response);

        } else {

            // render an object representation
            ContentTypeEngine engine = application.getContentTypeEngine(contentType);

            if (engine == null) {

                log.warn("No registered content type engine for '{}'", contentType);
                renderHtml(statusCode, request, response);

            } else {
                Error error = prepareError(statusCode, request, response);
                try {
                    response.send(error, engine.getContentType());
                } catch (Exception e) {
                    log.error("Unexpected error generating '{}' as '{}'!", Error.class.getName(), contentType, e);
                    response.status(HttpConstants.StatusCode.INTERNAL_ERROR);
                    response.text(application.getMessages().get("pippo.statusCode500", request, response));
                }
            }
        }
    }

    protected void renderHtml(int statusCode, Request request, Response response) {
        if (application.getTemplateEngine() == null) {
            renderDirectly(request, response);
        } else {
            String template = getTemplateForStatusCode(statusCode);
            if (template == null) {
                log.debug("There is no {} template for status code '{}'", application.getTemplateEngine().getClass()
                        .getSimpleName(), statusCode);
                renderDirectly(request, response);
            } else {
                try {
                    Error error = prepareError(statusCode, request, response);
                    Map<String, Object> bindings = error.asMap();
                    bindings.putAll(prepareTemplateBindings(statusCode, request, response));
                    response.getLocals().putAll(bindings);
                    response.render(template);
                } catch (Exception e) {
                    log.error("Unexpected error rendering your '{}' template!", template, e);
                    handle(e, request, response);
                }
            }
        }
    }

    @Override
    public void handle(Exception exception, Request request, Response response) {

        String message = exception.getMessage();
        if (!StringUtils.isNullOrEmpty(message)) {
            response.bind("message", message);
        }

        if (application.getPippoSettings().isDev()) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            exception.printStackTrace(printWriter);
            String stackTrace = stringWriter.toString();
            response.bind("stacktrace", stackTrace);
        }

        handle(HttpConstants.StatusCode.INTERNAL_ERROR, request, response);
    }

    /**
     * Render the result directly.
     *
     * @param request
     * @param response
     */
    protected void renderDirectly(Request request, Response response) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<div>");
        content.append("Cannot find a route for '");
        content.append(request.getMethod());
        content.append(" ");
        content.append(request.getUri());
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

        response.send(content);
    }

    /**
     * Get the template bindings for the error response.
     *
     * @param request
     * @param response
     * @return bindings map
     */
    protected Map<String, Object> prepareTemplateBindings(int statusCode, Request request, Response response) {
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
     * @param request
     * @param response
     * @return an Error
     */
    protected Error prepareError(int statusCode, Request request, Response response) {
        String messageKey = "pippo.statusCode" + statusCode;

        Error error = new Error();
        error.statusCode = statusCode;
        error.statusMessage = application.getMessages().get(messageKey, request, response);
        error.requestMethod = request.getMethod();
        error.requestUri = request.getContextUri();
        error.stacktrace = (String) response.getLocals().get("stacktrace");
        error.message = (String) response.getLocals().get("message");

        return error;
    }

    protected String getTemplateForStatusCode(int statusCode) {
        switch (statusCode) {
        case 404:
            return TemplateEngine.NOT_FOUND_404;
        default:
        case 500:
            return TemplateEngine.INTERNAL_ERROR_500;
        }
    }

}
