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

import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.core.controller.ControllerHandler;
import ro.fortsoft.pippo.core.controller.ControllerInitializationListenerList;
import ro.fortsoft.pippo.core.controller.ControllerInstantiationListenerList;
import ro.fortsoft.pippo.core.controller.ControllerInvokeListenerList;
import ro.fortsoft.pippo.core.route.ClasspathResourceHandler;
import ro.fortsoft.pippo.core.route.DefaultRouteMatcher;
import ro.fortsoft.pippo.core.route.DefaultRouteNotFoundHandler;
import ro.fortsoft.pippo.core.route.Route;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteMatcher;
import ro.fortsoft.pippo.core.route.RouteNotFoundHandler;
import ro.fortsoft.pippo.core.route.UrlBuilder;
import ro.fortsoft.pippo.core.util.HttpCacheToolkit;
import ro.fortsoft.pippo.core.util.MimeTypes;
import ro.fortsoft.pippo.core.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private PippoSettings pippoSettings;
    private Languages languages;
    private Messages messages;
    private MimeTypes mimeTypes;
    private HttpCacheToolkit httpCacheToolkit;
    private TemplateEngine templateEngine;
    private Map<String, ContentTypeEngine> engines;
    private RouteMatcher routeMatcher;
    private UrlBuilder urlBuilder;
    private ExceptionHandler exceptionHandler;
    private RouteNotFoundHandler routeNotFoundHandler;

    private String uploadLocation = System.getProperty("java.io.tmpdir");
    private long maximumUploadSize = -1L;

    private ControllerInstantiationListenerList controllerInstantiationListeners;
    private ControllerInitializationListenerList controllerInitializationListeners;
    private ControllerInvokeListenerList controllerInvokeListeners;

    private Map<String, Object> locals;

    /**
     * Get Application for current thread.
     *
     * @return The current thread's Application
     */
    public static Application get() {
        Application application = ThreadContext.getApplication();
        if (application == null) {
            throw new PippoRuntimeException("There is no application attached to current thread '{}'", Thread
                    .currentThread().getName());
        }

        return application;
    }

    public Application() {
        this(new PippoSettings(RuntimeMode.getCurrent()));
    }

    public Application(PippoSettings settings) {
        this.pippoSettings = settings;
        this.languages = new Languages(settings);
        this.messages = new Messages(languages);
        this.mimeTypes = new MimeTypes(settings);
        this.httpCacheToolkit = new HttpCacheToolkit(settings);
        this.engines = new TreeMap<>();
    }

    public void init() {
    }

    public void destroy() {
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
            log.debug("Template engine already registered, ignoring {}", engineClass.getName());
            return;
        }
        TemplateEngine engine = null;
        try {
            engine = engineClass.newInstance();
            setTemplateEngine(engine);
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to instantiate '{}'", e, engineClass.getName());
        }
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        templateEngine.init(this);
        this.templateEngine = templateEngine;
        log.debug("Template engine is {}", templateEngine.getClass().getName());

    }

    public Map<String, ContentTypeEngine> getContentTypeEngines() {
        return Collections.unmodifiableMap(engines);
    }

    public boolean hasContentTypeEngine(String contentType) {
        return engines.containsKey(contentType);
    }

    /**
     * Registers a content type engine if no other engine has been registered
     * for the content type.
     *
     * @param engineClass
     */
    public void registerContentTypeEngine(Class<? extends ContentTypeEngine> engineClass) {
        ContentTypeEngine engine = null;
        try {
            engine = engineClass.newInstance();
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to instantiate '{}'", e, engineClass.getName());
        }
        if (!engines.containsKey(engine.getContentType())) {
            setContentTypeEngine(engine);
        } else {
            log.debug("'{}' engine already registered, ignoring {}", engine.getContentType(), engineClass.getName());
        }
    }

    /**
     * Returns the first matching content type engine for a simple content type
     * or a complex accept header like:
     *
     * <pre>
     * text/html,application/xhtml+xml,application/xml;q=0.9,image/webp
     * </pre>
     *
     * @param contentType
     * @return null or the first matching content type engine
     */
    public ContentTypeEngine getContentTypeEngine(String contentType) {
        if (StringUtils.isNullOrEmpty(contentType)) {
            return null;
        }

        String[] types = contentType.split(",");
        for (String type : types) {
            if (type.contains(";")) {
                // drop ;q=0.8 quality scores
                type = type.substring(type.indexOf(';'));
            }

            ContentTypeEngine engine = engines.get(type);
            if (engine != null) {
                return engine;
            }
        }

        return null;
    }

    public void setContentTypeEngine(ContentTypeEngine engine) {
        engine.init(this);
        engines.put(engine.getContentType(), engine);
        log.debug("'{}' content engine is {}", engine.getContentType(), engine.getClass().getName());
    }

    public RouteMatcher getRouteMatcher() {
        if (routeMatcher == null) {
            routeMatcher = new DefaultRouteMatcher();
        }

        return routeMatcher;
    }

    public void setRouteMatcher(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
    }

    void setContextPath(String contextPath) {
        getUrlBuilder().setContextPath(contextPath);
    }

    public void GET(ClasspathResourceHandler resourceHandler) {
        if (getUrlBuilder().urlPatternFor(resourceHandler.getClass()) != null) {
            throw new PippoRuntimeException("You may only register one route for {}", resourceHandler.getClass()
                    .getSimpleName());
        }
        resourceHandler.setMimeTypes(mimeTypes);
        resourceHandler.setHttpCacheToolkit(httpCacheToolkit);
        addRoute(resourceHandler.getUrlPattern(), HttpConstants.Method.GET, resourceHandler);
    }

    public void GET(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.GET, routeHandler);
    }

    public void GET(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.GET, new ControllerHandler(controllerClass, methodName));
    }

    public void POST(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.POST, routeHandler);
    }

    public void POST(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.POST, new ControllerHandler(controllerClass, methodName));
    }

    public void DELETE(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.DELETE, routeHandler);
    }

    public void DELETE(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.DELETE, new ControllerHandler(controllerClass, methodName));
    }

    public void HEAD(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.HEAD, routeHandler);
    }

    public void HEAD(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.HEAD, new ControllerHandler(controllerClass, methodName));
    }

    public void PUT(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.PUT, routeHandler);
    }

    public void PUT(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.PUT, new ControllerHandler(controllerClass, methodName));
    }

    public void PATCH(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.PATCH, routeHandler);
    }

    public void PATCH(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.PATCH, new ControllerHandler(controllerClass, methodName));
    }

    public void addRoute(String urlPattern, String requestMethod, RouteHandler routeHandler) {
        Route route = new Route(urlPattern, requestMethod, routeHandler);
        try {
            getRouteMatcher().addRoute(route);
        } catch (Exception e) {
            log.error("Cannot add route '{}'", route, e);
        }
    }

    public ExceptionHandler getExceptionHandler() {
        if (exceptionHandler == null) {
            exceptionHandler = new DefaultExceptionHandler(this);
        }

        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public RouteNotFoundHandler getRouteNotFoundHandler() {
        if (routeNotFoundHandler == null) {
            routeNotFoundHandler = new DefaultRouteNotFoundHandler(this);
        }

        return routeNotFoundHandler;
    }

    public void setRouteNotFoundHandler(RouteNotFoundHandler routeNotFoundHandler) {
        this.routeNotFoundHandler = routeNotFoundHandler;
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

    public ControllerInstantiationListenerList getControllerInstantiationListeners() {
        if (controllerInstantiationListeners == null) {
            controllerInstantiationListeners = new ControllerInstantiationListenerList();
        }

        return controllerInstantiationListeners;
    }

    public ControllerInitializationListenerList getControllerInitializationListeners() {
        if (controllerInitializationListeners == null) {
            controllerInitializationListeners = new ControllerInitializationListenerList();
        }

        return controllerInitializationListeners;
    }

    public ControllerInvokeListenerList getControllerInvokeListeners() {
        if (controllerInvokeListeners == null) {
            controllerInvokeListeners = new ControllerInvokeListenerList();
        }

        return controllerInvokeListeners;
    }

    public Map<String, Object> getLocals() {
        if (locals == null) {
            locals = new HashMap<>();
        }

        return locals;
    }

    public UrlBuilder getUrlBuilder() {
        if (urlBuilder == null) {
            urlBuilder = new UrlBuilder(getRouteMatcher());
        }
        return urlBuilder;
    }

    @Override
    public String toString() {
        String toString = (getApplicationName() + " " + getApplicationVersion()).trim();
        if (toString.isEmpty()) {
            super.toString();
        }

        return toString;
    }

}
