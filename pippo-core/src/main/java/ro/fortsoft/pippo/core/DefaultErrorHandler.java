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

        String acceptType = request.getAcceptType();
        ContentTypeEngine engine = application.getContentTypeEngine(acceptType);

        if (engine == null) {
            log.warn("No registered content type engine for '{}'", acceptType);
            if (application.getTemplateEngine() == null) {
                renderDirectly(request, response);
            } else {
                String template = getTemplateForStatusCode(statusCode);
                if (template == null) {
                    log.debug("There is no {} template for status code '{}'",
                            application.getTemplateEngine().getClass().getSimpleName(), statusCode);
                    renderDirectly(request, response);
                } else {
                    try {
                        Map<String, Object> locals = prepareLocals(statusCode, request, response);
                        response.getLocals().putAll(locals);
                        response.render(template);
                    } catch (Exception e) {
                        log.error("Unexpected error rendering your '{}' template!", template, e);
                        renderDirectly(request, response);
                    }
                }
            }
        } else {
            try {
                Map<String, Object> locals = prepareLocals(statusCode, request, response);
                response.send(locals, engine.getContentType());
            } catch (Exception e) {
                log.error("Unexpected error rendering generating '{}' representation!", acceptType, e);
                renderDirectly(request, response);
            }
        }
    }

    @Override
    public void handle(Exception exception, Request request, Response response) {

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
            content.append(route.getUrlPattern());
            content.append("</li>");
        }
        content.append("</ul>");
        content.append("</body></html>");

        response.send(content);
    }

    /**
     * Get the local values for the error response.
     *
     * @param request
     * @param response
     * @return local values map
     */
    protected Map<String, Object> prepareLocals(int statusCode, Request request, Response response) {
        String messageKey = "pippo.statusCode" + statusCode;

        Map<String, Object> locals = new LinkedHashMap<>();
        locals.put("applicationName", application.getApplicationName());
        locals.put("applicationVersion", application.getApplicationVersion());
        locals.put("runtimeMode", application.getPippoSettings().getRuntimeMode());
        locals.put("statusCode", statusCode);
        locals.put("statusMessage", application.getMessages().get(messageKey, request, response));
        locals.put("requestMethod", request.getMethod());
        locals.put("requestUri", request.getUri());

        if (application.getPippoSettings().isDev()) {
            locals.put("routes", application.getRouter().getRoutes());
        }

        return locals;
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
