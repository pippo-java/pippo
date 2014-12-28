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
import ro.fortsoft.pippo.core.controller.ControllerHandlerFactory;
import ro.fortsoft.pippo.core.controller.ControllerInitializationListenerList;
import ro.fortsoft.pippo.core.controller.ControllerInstantiationListenerList;
import ro.fortsoft.pippo.core.controller.ControllerInvokeListenerList;
import ro.fortsoft.pippo.core.controller.DefaultControllerHandlerFactory;
import ro.fortsoft.pippo.core.route.ClasspathResourceHandler;
import ro.fortsoft.pippo.core.route.DefaultRouter;
import ro.fortsoft.pippo.core.route.Route;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.Router;
import ro.fortsoft.pippo.core.util.HttpCacheToolkit;
import ro.fortsoft.pippo.core.util.MimeTypes;
import ro.fortsoft.pippo.core.util.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

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
    private ContentTypeEngines engines;
    private Router router;
    private ErrorHandler errorHandler;
    private ControllerHandlerFactory controllerHandlerFactory;

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
        this.engines = new ContentTypeEngines();

        registerContentTypeEngine(TextPlainEngine.class);
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
            log.debug("Template engine already registered, ignoring '{}'", engineClass.getName());
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
        log.debug("Template engine is '{}'", templateEngine.getClass().getName());

    }

    public ContentTypeEngines getContentTypeEngines() {
        return engines;
    }

    public boolean hasContentTypeEngine(String contentType) {
        return engines.hasContentTypeEngine(contentType);
    }

    public void registerContentTypeEngine(Class<? extends ContentTypeEngine> engineClass) {
        engines.registerContentTypeEngine(engineClass);
    }

    public ContentTypeEngine getContentTypeEngine(String contentType) {
       return engines.getContentTypeEngine(contentType);
    }

    public void setContentTypeEngine(ContentTypeEngine engine) {
        engine.init(this);

        engines.setContentTypeEngine(engine);
    }

    public Router getRouter() {
        if (router == null) {
            router = new DefaultRouter();
        }

        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    void setContextPath(String contextPath) {
        getRouter().setContextPath(contextPath);
    }

    public void GET(ClasspathResourceHandler resourceHandler) {
        if (getRouter().uriPatternFor(resourceHandler.getClass()) != null) {
            throw new PippoRuntimeException("You may only register one route for {}",
                    resourceHandler.getClass().getSimpleName());
        }
        resourceHandler.setMimeTypes(mimeTypes);
        resourceHandler.setHttpCacheToolkit(httpCacheToolkit);
        addRoute(resourceHandler.getUrlPattern(), HttpConstants.Method.GET, resourceHandler);
    }

    public void GET(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.GET, routeHandler);
    }

    public void GET(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.GET, controllerClass, methodName);
    }

    public void POST(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.POST, routeHandler);
    }

    public void POST(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.POST, controllerClass, methodName);
    }

    public void DELETE(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.DELETE, routeHandler);
    }

    public void DELETE(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.DELETE, controllerClass, methodName);
    }

    public void HEAD(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.HEAD, routeHandler);
    }

    public void HEAD(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.HEAD, controllerClass, methodName);
    }

    public void PUT(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.PUT, routeHandler);
    }

    public void PUT(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.PUT, controllerClass, methodName);
    }

    public void PATCH(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.PATCH, routeHandler);
    }

    public void PATCH(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.PATCH, controllerClass, methodName);
    }

    public void addRoute(String urlPattern, String requestMethod, Class<? extends Controller> controllerClass, String methodName) {
        RouteHandler routeHandler = getControllerHandlerFactory().createHandler(controllerClass, methodName);
        addRoute(urlPattern, requestMethod, routeHandler);
    }

    public void addRoute(String urlPattern, String requestMethod, RouteHandler routeHandler) {
        Route route = new Route(urlPattern, requestMethod, routeHandler);
        try {
            getRouter().addRoute(route);
        } catch (Exception e) {
            log.error("Cannot add route '{}'", route, e);
        }
    }

    public ControllerHandlerFactory getControllerHandlerFactory() {
        if (controllerHandlerFactory == null) {
            ControllerHandlerFactory factory = ServiceLocator.locate(ControllerHandlerFactory.class);
            if (factory == null) {
                factory = new DefaultControllerHandlerFactory();
            }
            factory.init(this);
            controllerHandlerFactory = factory;
        }

        return controllerHandlerFactory;
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

    @Override
    public String toString() {
        String toString = (getApplicationName() + " " + getApplicationVersion()).trim();
        if (toString.isEmpty()) {
            super.toString();
        }

        return toString;
    }

}
