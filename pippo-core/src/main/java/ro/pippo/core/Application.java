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

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all Pippo applications.
 * From here, you can make some configuration changes or enhancements (for example: custom router,
 * custom request, custom response, ..) and add routes.
 *
 * @author Decebal Suiu
 */
public class Application implements ResourceRouting {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private PippoSettings pippoSettings;
    private Languages languages;
    private Messages messages;
    private MimeTypes mimeTypes;
    private HttpCacheToolkit httpCacheToolkit;
    private TemplateEngine templateEngine;
    private ContentTypeEngines engines;
    protected Router router;
    private ErrorHandler errorHandler;
    private RequestResponseFactory requestResponseFactory;
    private ServletContext servletContext;

    private List<Initializer> initializers;

    private String uploadLocation = System.getProperty("java.io.tmpdir");
    private long maximumUploadSize = -1L;

    private RoutePreDispatchListenerList routePreDispatchListeners;
    private RoutePostDispatchListenerList routePostDispatchListeners;

    private Map<String, Object> locals;
    private RouteHandler notFoundRouteHandler;

    private WebSocketRouter webSocketRouter;

    public Application() {
        this(new PippoSettings(RuntimeMode.getCurrent()));
    }

    public Application(PippoSettings settings) {
        this.pippoSettings = settings;
        this.languages = new Languages(settings);
        this.messages = new Messages(languages);
        this.mimeTypes = new MimeTypes(settings);
        this.httpCacheToolkit = new HttpCacheToolkit(settings);
        this.engines = new ContentTypeEngines();
        this.initializers = new ArrayList<>();
        this.webSocketRouter = new WebSocketRouter();

        registerContentTypeEngine(TextPlainEngine.class);
    }

    public final void init() {
        // add initializers
        initializers.addAll(ServiceLocator.locateAll(Initializer.class));

        // call each initializer
        for (Initializer initializer : initializers) {
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
        for (Initializer initializer : initializers) {
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
        return pippoSettings.getRuntimeMode();
    }

    public PippoSettings getPippoSettings() {
        return pippoSettings;
    }

    public String getApplicationName() {
        return pippoSettings.getString(PippoConstants.SETTING_APPLICATION_NAME, "");
    }

    public String getApplicationVersion() {
        return pippoSettings.getString(PippoConstants.SETTING_APPLICATION_VERSION, "");
    }

    public Languages getLanguages() {
        return languages;
    }

    public Messages getMessages() {
        return messages;
    }

    public MimeTypes getMimeTypes() {
        return mimeTypes;
    }

    public HttpCacheToolkit getHttpCacheToolkit() {
        return httpCacheToolkit;
    }

    /**
     * Registers a template engine if no other engine has been registered.
     *
     * @param engineClass
     */
    public void registerTemplateEngine(Class<? extends TemplateEngine> engineClass) {
        if (templateEngine != null) {
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
        return templateEngine;
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        templateEngine.init(this);
        this.templateEngine = templateEngine;
        log.debug("Template engine is '{}'", templateEngine.getClass().getName());
    }

    public ContentTypeEngines getContentTypeEngines() {
        return engines;
    }

    public boolean hasContentTypeEngine(String contentType) {
        return engines.hasContentTypeEngine(contentType);
    }

    public void registerContentTypeEngine(Class<? extends ContentTypeEngine> engineClass) {
        ContentTypeEngine engine = engines.registerContentTypeEngine(engineClass);
        if (engine != null) {
            engine.init(this);
        }
    }

    public ContentTypeEngine getContentTypeEngine(String contentType) {
        return engines.getContentTypeEngine(contentType);
    }

    public Router getRouter() {
        if (router == null) {
            router = new DefaultRouter();
        }

        return router;
    }

    public void setRouter(Router router) {
        setRouter(router, true);
    }

    public void setRouter(Router router, boolean preserveOldTransformers) {
        if (preserveOldTransformers && (router != null)) {
            // preserve route transformers already registered
            List<RouteTransformer> transformers = this.router.getRouteTransformers();
            transformers.forEach(router::addRouteTransformer);
        }

        this.router = router;
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
        if (errorHandler == null) {
            errorHandler = new DefaultErrorHandler(this);
        }

        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public final RequestResponseFactory getRequestResponseFactory() {
        if (requestResponseFactory == null) {
            requestResponseFactory = createRequestResponseFactory();
        }

        return requestResponseFactory;
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

    /**
     * The directory location where files will be stored.
     *
     * @return
     */
    public String getUploadLocation() {
        return uploadLocation;
    }

    public void setUploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;
    }

    /**
     * Gets the maximum size allowed for uploaded files.
     *
     * @return
     */
    public long getMaximumUploadSize() {
        return maximumUploadSize;
    }

    public void setMaximumUploadSize(long maximumUploadSize) {
        this.maximumUploadSize = maximumUploadSize;
    }

    public RoutePreDispatchListenerList getRoutePreDispatchListeners() {
        if (routePreDispatchListeners == null) {
            routePreDispatchListeners = new RoutePreDispatchListenerList();
        }

        return routePreDispatchListeners;
    }

    public RoutePostDispatchListenerList getRoutePostDispatchListeners() {
        if (routePostDispatchListeners == null) {
            routePostDispatchListeners = new RoutePostDispatchListenerList();
        }

        return routePostDispatchListeners;
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
    public void setNotFoundRouteHandler(RouteHandler routeHandler) {
        this.notFoundRouteHandler = routeHandler;
    }

    /**
     * Returns the {@code Catch-All} route handler or null.
     *
     * @return
     */
    public RouteHandler getNotFoundRouteHandler() {
        return notFoundRouteHandler;
    }

    public void addWebSocket(String uriPattern, WebSocketHandler webSocketHandler) {
        webSocketRouter.addRoute(uriPattern, webSocketHandler);
    }

    public WebSocketRouter getWebSocketRouter() {
        return webSocketRouter;
    }

    /**
     * Returns not null only in the context of the web layer (on a HTTP request).
     * It cannot be useful in a service (server side business layer).
     * For example if want to have access to PippoSettings from a service you must to inject PippoSettings
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
