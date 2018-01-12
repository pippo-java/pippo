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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code DefaultErrorHandler} is the core {@link ExceptionHandler} that integrates with
 * the {@link TemplateEngine} and {@link ContentTypeEngines}.
 * It generates a representation of an {@link Exception} or error result ({@link HttpConstants.StatusCode}).
 *
 * @author Decebal Suiu
 * @author James Moger
 */
public class DefaultErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultErrorHandler.class);

    private static final String MESSAGE = "message";
    private static final String STACKTRACE = "stacktrace";

    private Application application;

    private final Map<Class<? extends Exception>, ExceptionHandler> exceptionHandlers;

    public DefaultErrorHandler(Application application) {
        this.application = application;
        this.exceptionHandlers = new HashMap<>();

        setExceptionHandler(StatusCodeException.class, new StatusCodeExceptionHandler(this));
    }

    @Override
    public void setExceptionHandler(Class<? extends Exception> exceptionClass, ExceptionHandler exceptionHandler) {
        exceptionHandlers.put(exceptionClass, exceptionHandler);
    }

    /**
     * Returns an ExceptionHandler registered for this exception type.
     * If no handler is found it returns null and the default handle
     * (this class) is used.
     *
     * @param exception
     * @return an exception handler or null
     */
    @Override
    public ExceptionHandler getExceptionHandler(Exception exception) {
        Class<? extends Exception> exceptionClass = exception.getClass();
        if (!exceptionHandlers.containsKey(exceptionClass)) {
            Class<?> superClass = exceptionClass.getSuperclass();
            while (superClass != null) {
                if (exceptionHandlers.containsKey(superClass)) {
                    ExceptionHandler exceptionHandler = exceptionHandlers.get(superClass);
                    exceptionHandlers.put(exceptionClass, exceptionHandler);

                    return exceptionHandler;
                }

                superClass = superClass.getSuperclass();
            }

            // avoid superclass traversal in the future if we didn't find a handler
            exceptionHandlers.put(exceptionClass, null);
        }

        return exceptionHandlers.get(exceptionClass);
    }

    @Override
    public void handle(int statusCode, RouteContext routeContext) {
        checkForRecursion(routeContext);

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
                    routeContext.getResponse().contentType(contentType).send(error);
                } catch (Exception e) {
                    log.error("Unexpected error generating '{}' as '{}'", Error.class.getName(), contentType, e);
                    routeContext.status(HttpConstants.StatusCode.INTERNAL_ERROR);
                    routeContext.send(application.getMessages().get("pippo.statusCode500", routeContext));
                }
            }
        }
    }

    protected void renderHtml(int statusCode, RouteContext routeContext) {
        TemplateEngine engine = application.getTemplateEngine();
        if (engine == null) {
            renderDirectly(statusCode, routeContext);
        } else {
            String template = getTemplateForStatusCode(statusCode);
            if (template == null) {
                log.debug("There is no {} template for status code '{}'", engine.getClass().getSimpleName(), statusCode);
                renderDirectly(statusCode, routeContext);
            } else {
                try {
                    Error error = prepareError(statusCode, routeContext);
                    Map<String, Object> bindings = error.asMap();
                    bindings.putAll(prepareTemplateBindings(statusCode, routeContext));
                    routeContext.setLocals(bindings);
                    routeContext.render(template);
                } catch (Exception e) {
                    log.error("Unexpected error rendering '{}' template", template, e);
                    renderDirectly(statusCode, routeContext);
                }
            }
        }
    }

    @Override
    public void handle(Exception exception, RouteContext routeContext) {
        checkForRecursion(routeContext);

        if (exception instanceof PippoRuntimeException && exception.getCause() instanceof Exception) {
            handle((Exception) exception.getCause(), routeContext);
        } else {
            ExceptionHandler exceptionHandler = getExceptionHandler(exception);
            if (exceptionHandler != null) {
                log.debug("Handling '{}' with '{}'", exception.getClass().getSimpleName(), exceptionHandler.getClass().getName());
                exceptionHandler.handle(exception, routeContext);

                return;
            }

            log.error(exception.getMessage(), exception);

            if (routeContext.getResponse().isCommitted()) {
                log.debug("The response has already been committed. Cannot use the exception handler.");
                return;
            }

            String message = exception.getMessage();
            if (!StringUtils.isNullOrEmpty(message) && !routeContext.getResponse().getLocals().containsKey(MESSAGE)) {
                routeContext.setLocal(MESSAGE, message);
            }

            if (application.getPippoSettings().isDev()) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                exception.printStackTrace(printWriter);
                String stackTrace = stringWriter.toString();
                routeContext.setLocal(STACKTRACE, stackTrace);
            }

            handle(HttpConstants.StatusCode.INTERNAL_ERROR, routeContext);
        }
    }

    /**
     * Render the result directly (without template).
     *
     * @param routeContext
     */
    protected void renderDirectly(int statusCode, RouteContext routeContext) {
        if (application.getPippoSettings().isProd()) {
            routeContext.getResponse().commit();
        } else {
            if (statusCode == HttpConstants.StatusCode.NOT_FOUND) {
                StringBuilder content = new StringBuilder();
                content.append("<html><body>");
                content.append("<pre>");

                content.append("Cannot find a route for '");
                content.append(routeContext.getRequestMethod()).append(' ').append(routeContext.getRequestUri());
                content.append('\'');
                content.append('\n');

                content.append("Available routes:");
                content.append('\n');

                List<Route> routes = application.getRouter().getRoutes();
                for (Route route : routes) {
                    content.append('\t').append(route.getRequestMethod()).append(' ').append(route.getUriPattern());
                    content.append('\n');
                }

                content.append("</pre>");
                content.append("</body></html>");

                routeContext.send(content);
            } else if (statusCode == HttpConstants.StatusCode.INTERNAL_ERROR) {
                StringBuilder content = new StringBuilder();
                content.append("<html><body>");
                content.append("<pre>");

                Error error = prepareError(statusCode, routeContext);
                content.append(error.toString());

                content.append("</pre>");
                content.append("</body></html>");

                routeContext.send(content);
            }
        }
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
        error.setStatusCode(statusCode);
        error.setStatusMessage(application.getMessages().get(messageKey, routeContext));
        error.setRequestMethod(routeContext.getRequestMethod());
        error.setRequestUri(routeContext.getRequestUri());
        error.setStacktrace(routeContext.getLocal(STACKTRACE));
        error.setMessage(routeContext.getLocal(MESSAGE));

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

    private void checkForRecursion(RouteContext routeContext) {
        Integer depth = routeContext.removeLocal("__errorHandlerDepth");
        if (depth == null) {
            depth = 0;
        }
        depth += 1;
        routeContext.setLocal("__errorHandlerDepth", depth);
        if (depth > 4) {
            throw new PippoRuntimeException("Recursion in error handler");
        }
    }

}
