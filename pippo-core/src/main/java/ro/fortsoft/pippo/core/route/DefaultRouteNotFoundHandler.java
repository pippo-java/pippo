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
package ro.fortsoft.pippo.core.route;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.HttpConstants;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.TemplateEngine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public class DefaultRouteNotFoundHandler implements RouteNotFoundHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultRouteNotFoundHandler.class);

    private Application application;

    public DefaultRouteNotFoundHandler(Application application) {
        this.application = application;
    }

    @Override
    public void handle(String requestMethod, String requestUri, Request request, Response response) {
        response.status(HttpConstants.StatusCode.NOT_FOUND);

        if (application.getTemplateEngine() == null) {
            renderDirectly(requestMethod, requestUri, request, response);
        } else {
            try {
                renderTemplate(requestMethod, requestUri, request, response);
            } catch (Exception e) {
                log.error("Unexpected error rendering your '{}' template!", TemplateEngine.notFound_404, e);
                application.getExceptionHandler().handle(e, request, response);
            }
        }
    }

    /**
     * Render the result directly.
     *
     * @param requestMethod
     * @param requestUri
     * @param request
     * @param response
     */
    protected void renderDirectly(String requestMethod, String requestUri, Request request, Response response) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<div>");
        content.append("Cannot find a route for '");
        content.append(requestMethod);
        content.append(" ");
        content.append(requestUri);
        content.append("'</div>");
        content.append("<div>Available routes:</div>");
        content.append("<ul style=\" list-style-type: none; margin: 0; \">");
        List<Route> routes = application.getRouteMatcher().getRoutes();
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
     * Render the result with the template engine.
     *
     * @param requestMethod
     * @param requestUri
     * @param request
     * @param response
     */
    protected void renderTemplate(String requestMethod, String requestUri, Request request, Response response) {
        String messageKey = "pippo.statusCode" + HttpConstants.StatusCode.NOT_FOUND;

        response.bind("applicationName", application.getApplicationName());
        response.bind("applicationVersion", application.getApplicationVersion());
        response.bind("runtimeMode", application.getPippoSettings().getRuntimeMode());
        response.bind("statusCode", HttpConstants.StatusCode.NOT_FOUND);
        response.bind("statusMessage", application.getMessages().get(messageKey, request, response));
        response.bind("requestMethod", requestMethod);
        response.bind("requestUri", requestUri);
        response.render(TemplateEngine.notFound_404);
    }

}
