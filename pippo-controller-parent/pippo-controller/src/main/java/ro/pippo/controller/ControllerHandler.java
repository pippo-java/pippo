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
package ro.pippo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.controller.extractor.MethodParameterExtractor;
import ro.pippo.controller.util.ClassUtils;
import ro.pippo.controller.util.ControllerUtils;
import ro.pippo.core.ContentTypeEngines;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.Request;
import ro.pippo.core.route.DefaultRouteContext;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RouteMatch;
import ro.pippo.core.util.LangUtils;
import ro.pippo.core.util.StringUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * It's a {@link RouteHandler} that executes the controller's methods.
 *
 * @author Decebal Suiu
 * @author James Moger
 */
public class ControllerHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(ControllerHandler.class);

    private final Class<? extends Controller> controllerClass;
    private final Method controllerMethod;

    private final ControllerApplication application;

    private final List<String> declaredConsumes;
    private final List<String> declaredProduces;
    private final boolean isNoCache;

    private List<RouteHandler> interceptors;
    private MethodParameterExtractor[] extractors;

    private Controller controller;

    @SuppressWarnings("unchecked")
    public ControllerHandler(ControllerApplication application, Method controllerMethod) {
        this.application = application;

        this.controllerClass = (Class<? extends Controller>) controllerMethod.getDeclaringClass();
        this.controllerMethod = controllerMethod;

        ContentTypeEngines engines = application.getContentTypeEngines();

        this.declaredConsumes = ControllerUtils.getConsumes(controllerMethod);
        validateConsumes(engines.getContentTypes());

        this.declaredProduces = ControllerUtils.getProduces(controllerMethod);
        validateProduces(engines.getContentTypes());

        this.isNoCache = ClassUtils.getAnnotation(controllerMethod, NoCache.class) != null;

        initInterceptors();
        initExtractors();
    }

    @Override
    public void handle(RouteContext routeContext) {
        try {
            if (!canConsume(routeContext)) {
                routeContext.next();

                return;
            }

            log.trace("Processing '{}' interceptors", LangUtils.toString(controllerMethod));
            int preInterceptStatus = routeContext.getResponse().getStatus();
            processRouteInterceptors(routeContext);
            int postInterceptStatus = routeContext.getResponse().getStatus();
            if (routeContext.getResponse().isCommitted()) {
                log.debug("Response committed by interceptor");
                routeContext.next();

                return;
            } else if (preInterceptStatus != postInterceptStatus && postInterceptStatus >= 300) {
                log.debug("Interceptor set status code to {}, committing response",
                        routeContext.getResponse().getStatus());
                routeContext.getResponse().commit();
                routeContext.next();

                return;
            }

            log.trace("Preparing '{}' parameters from request", LangUtils.toString(controllerMethod));
            Object[] values = prepareMethodParameters(routeContext);

            log.trace("Invoking '{}'", LangUtils.toString(controllerMethod));

            // create the controller instance
            Controller controller = getController();

            specifyCacheControls(routeContext);
            specifyContentType(routeContext);

            Object result = controllerMethod.invoke(controller, values);

            if (routeContext.getResponse().isCommitted()) {
                log.debug("Response committed in {}", LangUtils.toString(controllerMethod));
            } else {
                if (!controllerMethod.getReturnType().equals(Void.TYPE)) {
                    // method declares a Return Type
                    if (result == null) {
                        // NULL result, prepare a NOT FOUND (404)
                        routeContext.getResponse().notFound();
                    } else {
                        // send returned result
                        if (result instanceof CharSequence) {
                            // send a char sequence (e.g. pre-formatted JSON, XML, YAML, etc)
                            CharSequence charSequence = (CharSequence) result;
                            routeContext.send(charSequence);
                        } else if (result instanceof File) {
                            // stream a File resource
                            File file = (File) result;
                            routeContext.send(file);
                        } else {
                            // send an object using a ContentTypeEngine
                            routeContext.send(result);
                        }
                    }
                }
            }

            routeContext.next();
        } catch (InvocationTargetException e) {
            // handles exceptions thrown within the proxied controller method
            Throwable t = e.getTargetException();
            if (t instanceof Exception) {
                Exception target = (Exception) t;
                handleDeclaredThrownException(target, routeContext);
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                log.error("Failed to handle controller method exception", t);
            }
        } catch (Exception e) {
            // handles exceptions thrown within this handle() method
            handleDeclaredThrownException(e, routeContext);
        }
    }

    protected Controller getController() {
        if (controller == null) {
            return application.getControllerFactory().createController(controllerClass);
        }

        return controller;
    }

    protected void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Init interceptors from controller method.
     */
    protected void initInterceptors() {
        interceptors = new ArrayList<>();
        ControllerUtils.collectRouteInterceptors(controllerMethod).forEach(handlerClass -> {
            try {
                interceptors.add(handlerClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new PippoRuntimeException(e);
            }
        });
    }

    /**
     * Init extractors from controller method.
     */
    protected void initExtractors() {
        Parameter[] parameters = controllerMethod.getParameters();
        extractors = new MethodParameterExtractor[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = new MethodParameter(controllerMethod, i);
            MethodParameterExtractor extractor = application.getExtractors().stream()
                .filter(e -> e.isApplicable(parameter))
                .findFirst()
                .orElse(null);

            if (extractor == null) {
                throw new PippoRuntimeException(
                    "Method '{}' parameter {} of type '{}' does not specify a extractor",
                    LangUtils.toString(controllerMethod), i + 1, parameter.getParameterType());
            }

            extractors[i] = extractor;
        }
    }

    /**
     * Validates that the declared consumes can actually be processed by Pippo.
     *
     * @param contentTypes
     */
    protected void validateConsumes(Collection<String> contentTypes) {
        Set<String> ignoreConsumes = new TreeSet<>();
        ignoreConsumes.add(Consumes.ALL);

        // these are handled by the TemplateEngine
        ignoreConsumes.add(Consumes.HTML);
        ignoreConsumes.add(Consumes.XHTML);

        // these are handled by the Servlet layer
        ignoreConsumes.add(Consumes.FORM);
        ignoreConsumes.add(Consumes.MULTIPART);

        for (String declaredConsume : declaredConsumes) {
            if (ignoreConsumes.contains(declaredConsume)) {
                continue;
            }

            String consume = declaredConsume;
            int fuzz = consume.indexOf('*');
            if (fuzz > -1) {
                // strip fuzz, we must have a registered engine for the unfuzzed content-type
                consume = consume.substring(0, fuzz);
            }

            if (!contentTypes.contains(consume)) {
                if (consume.equals(declaredConsume)) {
                    throw new PippoRuntimeException("{} declares @{}(\"{}\") but there is no registered ContentTypeEngine for that type!",
                            LangUtils.toString(controllerMethod), Consumes.class.getSimpleName(), declaredConsume);
                } else {
                    throw new PippoRuntimeException("{} declares @{}(\"{}\") but there is no registered ContentTypeEngine for \"{}\"!",
                            LangUtils.toString(controllerMethod), Consumes.class.getSimpleName(), declaredConsume, consume);
                }
            }
        }
    }

    /**
     * Validates that the declared content-types can actually be generated by Pippo.
     *
     * @param contentTypes
     */
    protected void validateProduces(Collection<String> contentTypes) {
        Set<String> ignoreProduces = new TreeSet<>();
        ignoreProduces.add(Produces.TEXT);
        ignoreProduces.add(Produces.HTML);
        ignoreProduces.add(Produces.XHTML);

        for (String produces : declaredProduces) {
            if (ignoreProduces.contains(produces)) {
                continue;
            }

            if (!contentTypes.contains(produces)) {
                throw new PippoRuntimeException("{} declares @{}(\"{}\") but there is no registered ContentTypeEngine for that type!",
                        LangUtils.toString(controllerMethod), Produces.class.getSimpleName(), produces);
            }
        }
    }

    /**
     * Determines if the incoming request is sending content this route understands.
     *
     * @param routeContext
     * @return true if the route handles the request accept/content-type
     */
    protected boolean canConsume(RouteContext routeContext) {
        Set<String> contentTypes = getContentTypes(routeContext.getRequest());

        if (!declaredConsumes.isEmpty()) {
            if (declaredConsumes.contains(Consumes.ALL)) {
                log.debug("{} will handle Request because it consumes '{}'", LangUtils.toString(controllerMethod), Consumes.ALL);
                return true;
            }

            Set<String> types = new LinkedHashSet<>(contentTypes);
            if (types.isEmpty()) {
                // request does not specify a Content-Type so add Accept type(s)
                types.addAll(getAcceptTypes(routeContext.getRequest()));

                // request can handle any type, so consume the Request
                if (types.contains("*") || types.contains("*/*")) {
                    log.debug("{} will handle Request because it consumes '{}'", LangUtils.toString(controllerMethod), "*/*");
                    return true;
                }
            }

            for (String type : types) {
                if (declaredConsumes.contains(type)) {
                    // explicit content-type match
                    log.debug("{} will handle Request because it consumes '{}'", LangUtils.toString(controllerMethod), type);
                    return true;
                } else {
                    // look for a fuzzy content-type match
                    for (String declaredType : declaredConsumes) {
                        int fuzz = declaredType.indexOf('*');
                        if (fuzz > -1) {
                            String fuzzyType = declaredType.substring(0, fuzz);
                            if (type.startsWith(fuzzyType)) {
                                log.debug("{} will handle Request because it consumes '{}'", LangUtils.toString(controllerMethod), type);
                                return true;
                            }
                        }
                    }
                }
            }

            if (types.isEmpty()) {
                log.warn("{} can not handle Request because neither 'Accept' nor 'Content-Type' are set and Route @Consumes '{}'",
                    LangUtils.toString(controllerMethod), declaredConsumes);
            } else {
                log.warn("{} can not handle Request for '{}' because Route @Consumes '{}'", LangUtils.toString(controllerMethod),
                        types, declaredConsumes);
            }
            return false;
        }

        return true;
    }

    protected void processRouteInterceptors(RouteContext routeContext) {
        if (interceptors.isEmpty()) {
            return;
        }

        List<RouteMatch> chain = new ArrayList<>();
        for (RouteHandler interceptor : interceptors) {
            // create a route for interceptor
            Route route = new Route(routeContext.getRequestMethod(), routeContext.getRequestUri(), interceptor);
            route.setName(StringUtils.format("{}<{}>", Interceptor.class.getSimpleName(),
                    route.getRouteHandler().getClass().getSimpleName()));
            route.bindAll(routeContext.getRoute().getAttributes());

            // add route in chain
            RouteMatch match = new RouteMatch(route, null);
            chain.add(match);
        }

        // TODO DefaultRouteContext is hardcoded
        RouteContext context = new DefaultRouteContext(routeContext.getApplication(),
            routeContext.getRequest(),
            routeContext.getResponse(),
            chain);
        context.next();
    }

    protected Object[] prepareMethodParameters(RouteContext routeContext) {
        Parameter[] parameters = controllerMethod.getParameters();

        if (parameters.length == 0) {
            return new Object[0];
        }

        Object[] values = new Object[parameters.length];
        for (int i = 0; i < values.length; i++) {
            MethodParameter parameter = new MethodParameter(controllerMethod, i);
            Class<?> type = parameter.getParameterType();

            MethodParameterExtractor extractor = extractors[i];
            Object value = extractor.extract(parameter, routeContext);

            if (value == null || ClassUtils.isAssignable(value, type)) {
                values[i] = value;
            } else {
                String parameterName = parameter.getParameterName();
                throw new PippoRuntimeException("Type for '{}' is actually '{}' but was specified as '{}'!",
                        parameterName, value.getClass().getName(), type.getName());
            }
        }

        return values;
    }

    /**
     * Specify Response cache controls.
     *
     * @param routeContext
     */
    protected void specifyCacheControls(RouteContext routeContext) {
        if (isNoCache) {
            log.debug("NoCache detected, response may not be cached");
            routeContext.getResponse().noCache();
        }
    }

    /**
     * Specify the Response content-type by...
     * <ol>
     * <li>setting the first Produces content type</li>
     * <li>negotiating with the Request if multiple content-types are specified in Produces</li>
     * </ol>
     *
     * @param routeContext
     */
    protected void specifyContentType(RouteContext routeContext) {
        if (!declaredProduces.isEmpty()) {
            // Specify first Produces content-type
            String defaultContentType = declaredProduces.get(0);
            routeContext.getResponse().contentType(defaultContentType);

            if (declaredProduces.size() > 1) {
                // negotiate content-type from Request Accept/Content-Type
                routeContext.negotiateContentType();
            }
        }
    }

    protected void handleDeclaredThrownException(Exception e, RouteContext routeContext) {
        if (e instanceof RuntimeException) {
            // pass-through the thrown exception
            throw (RuntimeException) e;
        }

        // undeclared exception, wrap & throw
        throw new PippoRuntimeException(e);
    }

    private Set<String> getAcceptTypes(Request request) {
        Set<String> types = new LinkedHashSet<>();
        types.addAll(getContentTypes(request.getAcceptType()));
        types.addAll(getContentTypes(request.getHttpServletRequest().getHeader(HttpConstants.Header.ACCEPT)));

        return types;
    }

    private Set<String> getContentTypes(Request request) {
        Set<String> types = new LinkedHashSet<>();
        types.addAll(getContentTypes(request.getContentType()));
        types.addAll(getContentTypes(request.getHttpServletRequest().getContentType()));

        return types;
    }

    /*
     * Cleans a complex content-type or accept header value by removing the
     * quality scores.
     * <p/>
     * <pre>
     * text/html,application/xhtml+xml,application/xml;q=0.9,image/webp
     * </pre>
     *
     * @param contentType
     * @return the sanitized set of content-types
     */
    private Set<String> getContentTypes(String contentType) {
        if (StringUtils.isNullOrEmpty(contentType)) {
            return Collections.emptySet();
        }

        Set<String> set = new LinkedHashSet<>();
        String[] types = contentType.split(",");
        for (String type : types) {
            if (type.contains(";")) {
                // drop ;q=0.8 quality scores
                type = type.substring(0, type.indexOf(';'));
            }

            set.add(type.trim().toLowerCase());
        }

        return set;
    }

}
