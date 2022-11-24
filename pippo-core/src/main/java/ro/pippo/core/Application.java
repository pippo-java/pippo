/*
 * Copyright (C) 2014-present the original author or authors.
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
import ro.pippo.core.entity.DefaultEntityRequestEngine;
import ro.pippo.core.entity.EntityRequestEngine;
import ro.pippo.core.gzip.GZipRequestResponseFactory;
import ro.pippo.core.route.DefaultRouter;
import ro.pippo.core.route.ResourceRouting;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteDispatcher;
import ro.pippo.core.route.RouteGroup;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RoutePostDispatchListenerList;
import ro.pippo.core.route.RoutePreDispatchListenerList;
import ro.pippo.core.route.RouteTransformer;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.HttpCacheToolkit;
import ro.pippo.core.util.MimeTypes;
import ro.pippo.core.util.ServiceLocator;
import ro.pippo.core.websocket.WebSocketHandler;
import ro.pippo.core.websocket.WebSocketRouter;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Base class for all Pippo applications.
 * From here, you can make some configuration changes or enhancements (for example: custom router,
 * custom request, custom response, ..) and add routes.
 *
 * @author Decebal Suiu
 */
public class Application implements ResourceRouting {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Inject
    private Optional<Languages> languages = Optional.empty();

    @Inject
    private Optional<Messages> messages = Optional.empty();

    @Inject
    private Optional<MimeTypes> mimeTypes = Optional.empty();

    @Inject
    private Optional<HttpCacheToolkit> httpCacheToolkit = Optional.empty();

    @Inject
    private Optional<ContentTypeEngines> engines = Optional.empty();

    @Inject
    private Optional<EntityRequestEngine> entityRequestEngine = Optional.empty();

    @Inject
    private Optional<RequestResponseFactory> requestResponseFactory = Optional.empty();

    @Inject
    private Optional<List<Initializer>> initializers = Optional.empty();

    @Inject
    private Optional<RoutePreDispatchListenerList> routePreDispatchListeners = Optional.empty();

    @Inject
    private Optional<RoutePostDispatchListenerList> routePostDispatchListeners = Optional.empty();

    @Inject
    private Optional<WebSocketRouter > webSocketRouter = Optional.empty();

    @Inject
    private Optional<TemplateEngine> templateEngine = Optional.empty();

    @Inject
    private Optional<RouteHandler<?>> notFoundRouteHandler = Optional.empty();

    @Inject
    private Optional<Router> router = Optional.empty();

    @Inject
    private Optional<ErrorHandler> errorHandler = Optional.empty();

    private PippoSettings pippoSettings;
    private ServletContext servletContext;
    private Map<String, Object> locals;

    public Application() {
        this(new PippoSettings(RuntimeMode.getCurrent()));
    }

    @Inject
    public Application(PippoSettings settings) {
        pippoSettings = settings;
    }

    public final void init() {
        // call each initializer
        for (Initializer initializer : getInitializers()) {
            log.debug("Initializing '{}'", initializer.getClass().getName());
            try {
                initializer.init(this);
            } catch (Exception e) {
                log.error("Failed to initialize '{}'", initializer.getClass().getName(), e);
            }
        }

        // add transformers
        List<RouteTransformer> transformers = ServiceLocator.locateAll(RouteTransformer.class);
        for (RouteTransformer transformer : transformers) {
            getRouter().addRouteTransformer(transformer);
        }

        onInit();

        // compile routes
        getRouter().compileRoutes();
    }

    public final void destroy() {
        onDestroy();
        for (Initializer initializer : getInitializers()) {
            log.debug("Destroying '{}'", initializer.getClass().getName());
            try {
                initializer.destroy(this);
            } catch (Exception e) {
                log.error("Failed to destroy '{}'", initializer.getClass().getName(), e);
            }
        }
    }

    protected void onInit() {
    }

    protected void onDestroy() {
    }

    /**
     * The runtime mode. Must currently be either DEV, TEST, or PROD.
     */
    public RuntimeMode getRuntimeMode() {
        return getPippoSettings().getRuntimeMode();
    }

    public PippoSettings getPippoSettings() {
        if (pippoSettings == null) {
            pippoSettings = new PippoSettings(RuntimeMode.getCurrent());
        }

        return pippoSettings;
    }

    public String getApplicationName() {
        return getPippoSettings().getString(PippoConstants.SETTING_APPLICATION_NAME, "");
    }

    public String getApplicationVersion() {
        return getPippoSettings().getString(PippoConstants.SETTING_APPLICATION_VERSION, "");
    }

    public Languages getLanguages() {
        if (!languages.isPresent()) {
            languages = Optional.of(new Languages(getPippoSettings()));
        }

        return languages.get();
    }

    public Messages getMessages() {
        if (!messages.isPresent()) {
            messages = Optional.of(new Messages(getLanguages()));
        }

        return messages.get();
    }

    public MimeTypes getMimeTypes() {
        if (!mimeTypes.isPresent()) {
            mimeTypes = Optional.of(new MimeTypes(getPippoSettings()));
        }

        return mimeTypes.get();
    }

    public HttpCacheToolkit getHttpCacheToolkit() {
        if (!httpCacheToolkit.isPresent()) {
            httpCacheToolkit = Optional.of(new HttpCacheToolkit(getPippoSettings()));
        }

        return httpCacheToolkit.get();
    }

    /**
     * Registers a template engine if no other engine has been registered.
     *
     * @param engineClass
     */
    public void registerTemplateEngine(Class<? extends TemplateEngine> engineClass) {
        if (templateEngine.isPresent()) {
            log.debug("Template engine already registered, ignoring '{}'", engineClass.getName());
            return;
        }

        try {
            TemplateEngine engine = engineClass.newInstance();
            setTemplateEngine(engine);
        } catch (Exception e) {
            throw new PippoRuntimeException(e, "Failed to instantiate '{}'", engineClass.getName());
        }
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine.orElse(null);
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        templateEngine.init(this);
        this.templateEngine = Optional.of(templateEngine);
        log.debug("Template engine is '{}'", templateEngine.getClass().getName());
    }

    public ContentTypeEngines getContentTypeEngines() {
        if (!engines.isPresent()) {
            engines = Optional.of(new ContentTypeEngines());
            registerContentTypeEngine(TextPlainEngine.class);
        }

        return engines.get();
    }

    public boolean hasContentTypeEngine(String contentType) {
        return getContentTypeEngines().hasContentTypeEngine(contentType);
    }

    public void registerContentTypeEngine(Class<? extends ContentTypeEngine> engineClass) {
        ContentTypeEngine engine = getContentTypeEngines().registerContentTypeEngine(engineClass);
        if (engine != null) {
            engine.init(this);
        }
    }

    public ContentTypeEngine getContentTypeEngine(String contentType) {
        return getContentTypeEngines().getContentTypeEngine(contentType);
    }

    public EntityRequestEngine getEntityRequestEngine() {
        if (!entityRequestEngine.isPresent()) {
            entityRequestEngine = Optional.of(new DefaultEntityRequestEngine(getContentTypeEngines()));
        }

        return entityRequestEngine.get();
    }

    public void setEntityRequestEngine(EntityRequestEngine entityRequestEngine) {
        this.entityRequestEngine = Optional.of(entityRequestEngine);
    }

    public Router getRouter() {
        if (!router.isPresent()) {
            router = Optional.of(new DefaultRouter());
        }

        return router.get();
    }

    public void setRouter(Router router) {
        setRouter(router, true);
    }

    public void setRouter(Router router, boolean preserveOldTransformers) {
        Objects.requireNonNull(router);
        if (preserveOldTransformers) {
            // preserve route transformers already registered
            List<RouteTransformer> transformers = getRouter().getRouteTransformers();
            transformers.forEach(router::addRouteTransformer);
        }

        this.router = Optional.of(router);
    }

    @Override
    public void addRoute(Route route) {
        getRouter().addRoute(route);
    }

    @Override
    public void addRouteGroup(RouteGroup routeGroup) {
        getRouter().addRouteGroup(routeGroup);
    }

    public ErrorHandler getErrorHandler() {
        if (!errorHandler.isPresent()) {
            errorHandler = Optional.of(new DefaultErrorHandler(this));
        }

        return errorHandler.get();
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = Optional.of(errorHandler);
    }

    public final RequestResponseFactory getRequestResponseFactory() {
        if (!requestResponseFactory.isPresent()) {
            requestResponseFactory = Optional.of(createRequestResponseFactory());
        }

        return requestResponseFactory.get();
    }

    /**
     * Override this method if you want a custom RequestResponseFactory.
     *
     * @return
     */
    protected RequestResponseFactory createRequestResponseFactory() {
//        return new RequestResponseFactory(this);
        return new GZipRequestResponseFactory(this);
    }

    public RoutePreDispatchListenerList getRoutePreDispatchListeners() {
        if (!routePreDispatchListeners.isPresent()) {
            routePreDispatchListeners = Optional.of(new RoutePreDispatchListenerList());
        }

        return routePreDispatchListeners.get();
    }

    public RoutePostDispatchListenerList getRoutePostDispatchListeners() {
        if (!routePostDispatchListeners.isPresent()) {
            routePostDispatchListeners = Optional.of(new RoutePostDispatchListenerList());
        }

        return routePostDispatchListeners.get();
    }

    public Map<String, Object> getLocals() {
        if (locals == null) {
            locals = new HashMap<>();
        }

        return locals;
    }

    /**
     * Returns the servlet context for this application.
     * The servlet context is available after instantiation, so DON'T use this method in constructor
     * because it returns null.
     *
     * @return The servlet context or null
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Helper method that calls {@code getRouter().addRouteTransformer(transformer)}.
     *
     * @param transformer
     */
    public void addRouteTransformer(RouteTransformer transformer) {
        getRouter().addRouteTransformer(transformer);
    }

    /**
     * Set the {@link RouteHandler} that is called only if no route has been found for a request.
     * It's named {@code Catch-All} route handler.
     *
     * @param routeHandler
     */
    public void setNotFoundRouteHandler(RouteHandler<?> routeHandler) {
        this.notFoundRouteHandler = Optional.of(routeHandler);
    }

    /**
     * Returns the {@code Catch-All} route handler or null.
     *
     * @return
     */
    public RouteHandler<?> getNotFoundRouteHandler() {
        return notFoundRouteHandler.orElse(null);
    }

    public void addWebSocket(String uriPattern, WebSocketHandler webSocketHandler) {
        getWebSocketRouter().addRoute(uriPattern, webSocketHandler);
    }

    public WebSocketRouter getWebSocketRouter() {
        if (!webSocketRouter.isPresent()) {
            webSocketRouter = Optional.of(new WebSocketRouter());
        }

        return webSocketRouter.get();
    }

    public List<Initializer> getInitializers() {
        if (!initializers.isPresent()) {
            initializers = Optional.of(ServiceLocator.locateAll(Initializer.class));
        }

        return initializers.get();
    }

    /**
     * Returns not null only in the context of the web layer (on a HTTP request).
     * It cannot be useful in a service (server side business layer).
     * For example if you want to have access to PippoSettings from a service you must inject PippoSettings
     * in that service and not to use Application.get().getPippoSettings().
     *
     * @return The application instance or null
     */
    public static Application get() {
        RouteContext routeContext = RouteDispatcher.getRouteContext();

        return (routeContext != null) ? routeContext.getApplication() : null;
    }

    @Override
    public String toString() {
        String toString = (getApplicationName() + " " + getApplicationVersion()).trim();

        return toString.isEmpty() ? super.toString() : toString;
    }

}
