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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    private Application application;

    public DefaultExceptionHandler(Application application) {
        this.application = application;
    }

    @Override
    public void handle(Exception exception, Request request, Response response) {
        response.status(HttpConstants.StatusCode.INTERNAL_ERROR);

        if (application.getTemplateEngine() == null) {
            renderDirectly(exception, request, response);
        } else {
            try {
                renderTemplate(exception, request, response);
            } catch (Exception e) {
                log.error("Unexpected error rendering your '{}' template!", TemplateEngine.INTERNAL_ERROR_500, e);
                renderDirectly(exception, request, response);
            }
        }
    }

    /**
     * Render the exception directly.
     *
     * @param exception
     * @param request
     * @param response
     */
    protected void renderDirectly(Exception exception, Request request, Response response) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();

        StringBuilder content = new StringBuilder();
        content.append("<html><body><pre>");
        content.append(stackTrace);
        content.append("</pre></body></html>");
        response.send(content);
    }

    /**
     * Render the exception with the template engine.
     *
     * @param exception
     * @param request
     * @param response
     */
   protected void renderTemplate(Exception exception, Request request, Response response) {
       StringWriter stringWriter = new StringWriter();
       PrintWriter printWriter = new PrintWriter(stringWriter);
       exception.printStackTrace(printWriter);
       String stackTrace = stringWriter.toString();
       String messageKey = "pippo.statusCode" + HttpConstants.StatusCode.INTERNAL_ERROR;

       response.bind("applicationName", application.getApplicationName());
       response.bind("applicationVersion", application.getApplicationVersion());
       response.bind("runtimeMode", application.getPippoSettings().getRuntimeMode());
       response.bind("statusCode", HttpConstants.StatusCode.INTERNAL_ERROR);
       response.bind("statusMessage", application.getMessages().get(messageKey, request, response));
       response.bind("requestMethod", request.getMethod());
       response.bind("requestUri", request.getUri());
       if (application.getPippoSettings().isDev()) {
           response.bind("stacktrace", stackTrace);
       }
       response.render(TemplateEngine.INTERNAL_ERROR_500);
   }

}
