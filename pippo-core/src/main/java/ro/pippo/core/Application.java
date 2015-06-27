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
import ro.pippo.core.route.ClasspathResourceHandler;
import ro.pippo.core.route.DefaultRouter;
import ro.pippo.core.route.FileResourceHandler;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.ResourceHandler;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteDispatcher;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RoutePostDispatchListenerList;
import ro.pippo.core.route.RoutePreDispatchListenerList;
import ro.pippo.core.route.Router;
import ro.pippo.core.route.WebjarsResourceHandler;
import ro.pippo.core.util.HttpCacheToolkit;
import ro.pippo.core.util.MimeTypes;
import ro.pippo.core.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
    protected Router router;
    private ErrorHandler errorHandler;
    private RequestResponseFactory requestResponseFactory;

    private List<Initializer> initializers;

    private String uploadLocation = System.getProperty("java.io.tmpdir");
    private long maximumUploadSize = -1L;

    private RoutePreDispatchListenerList routePreDispatchListeners;
    private RoutePostDispatchListenerList routePostDispatchListeners;

    private Map<String, Object> locals;

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

        registerContentTypeEngine(TextPlainEngine.class);
    }

    public final void init() {
        initializers.addAll(getInitializers());
        for (Initializer initializer : initializers) {
            log.debug("Initializing '{}'", initializer.getClass().getName());
            try {
                initializer.init(this);
            } catch (Exception e) {
                log.error("Failed to initialize '{}'", initializer.getClass().getName(), e);
            }
        }

        onInit();
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
        this.router = router;
    }

    public Route GET(String uriPattern, RouteHandler routeHandler) {
        if (routeHandler instanceof ResourceHandler) {
            throw new PippoRuntimeException("Please use 'addResourceRoute()'");
        }

        return addRoute(uriPattern, HttpConstants.Method.GET, routeHandler);
    }

    public Route POST(String uriPattern, RouteHandler routeHandler) {
        return addRoute(uriPattern, HttpConstants.Method.POST, routeHandler);
    }

    public Route DELETE(String uriPattern, RouteHandler routeHandler) {
        return addRoute(uriPattern, HttpConstants.Method.DELETE, routeHandler);
    }

    public Route HEAD(String uriPattern, RouteHandler routeHandler) {
        return addRoute(uriPattern, HttpConstants.Method.HEAD, routeHandler);
    }

    public Route PUT(String uriPattern, RouteHandler routeHandler) {
        return addRoute(uriPattern, HttpConstants.Method.PUT, routeHandler);
    }

    public Route PATCH(String uriPattern, RouteHandler routeHandler) {
        return addRoute(uriPattern, HttpConstants.Method.PATCH, routeHandler);
    }

    public Route ALL(String uriPattern, RouteHandler routeHandler) {
        return addRoute(uriPattern, HttpConstants.Method.ALL, routeHandler);
    }

    public Route addRoute(String uriPattern, String requestMethod, RouteHandler routeHandler) {
        Route route = new Route(uriPattern, requestMethod, routeHandler);
        getRouter().addRoute(route);

        return route;
    }

    /**
     * It's a shortcut for {@link #addPublicResourceRoute(String)} with parameter <code>"/public"</code>.
     */
    public Route addPublicResourceRoute() {
        return addPublicResourceRoute("/public");
    }

    /**
     * Add a route that serves resources from the "public" directory within your classpath.
     */
    public Route addPublicResourceRoute(String urlPath) {
        return addResourceRoute(new PublicResourceHandler(urlPath));
    }

    /**
     * Add a route that serves resources from a directory(file system).
     */
    public Route addFileResourceRoute(String urlPath, File directory) {
        return addResourceRoute(new FileResourceHandler(urlPath, directory));
    }

    public Route addFileResourceRoute(String urlPath, String directory) {
        return addResourceRoute(new FileResourceHandler(urlPath, directory));
    }

    public Route addClasspathResourceRoute(String urlPath, Class<?> resourceClass) {
        return addResourceRoute(new ClasspathResourceHandler(urlPath, resourceClass.getName().replace(".", "/")));
    }

    /**
     * Add a route that serves resources from classpath.
     */
    public Route addClasspathResourceRoute(String urlPath, String resourceBasePath) {
        return addResourceRoute(new ClasspathResourceHandler(urlPath, resourceBasePath));
    }

    /**
     * It's a shortcut for {@link #addWebjarsResourceRoute(String)} with parameter <code>"/webjars"</code>.
     */
    public Route addWebjarsResourceRoute() {
        return addWebjarsResourceRoute("/webjars");
    }

    /**
     * Add a route that serves webjars (http://www.webjars.org/) resources.
     */
    public Route addWebjarsResourceRoute(String urlPath) {
        return addResourceRoute(new WebjarsResourceHandler(urlPath));
    }

    public Route addResourceRoute(ResourceHandler resourceHandler) {
        return addRoute(resourceHandler.getUriPattern(), HttpConstants.Method.GET, resourceHandler);
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
        return new RequestResponseFactory(this);
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

    public static Application get() {
        return RouteDispatcher.getRouteContext().getApplication();
    }

    private List<Initializer> getInitializers() {
        try {
            List<Initializer> initializers = new ArrayList<>();

            ClassLoader classLoader = getClass().getClassLoader();
            Enumeration<URL> resources = classLoader.getResources("pippo.properties");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                log.debug("Read '{}'", resource.getFile());
                Properties properties = new Properties();
                try (Reader reader = new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                } catch (IOException e) {
                    log.error("Failed to read '{}'", resource.getFile(), e);
                    continue;
                }

                String initializerClassName = properties.getProperty("initializer");
                if (StringUtils.isNullOrEmpty(initializerClassName)) {
                    log.warn("'{}' does not specify an 'initializer' setting!", resource.getFile());
                } else {
                    log.debug("Found initializer '{}'", initializerClassName);
                    Class<Initializer> initializerClass = (Class<Initializer>) classLoader.loadClass(initializerClassName);

                    Initializer initializer = initializerClass.newInstance();
                    initializers.add(initializer);
                }
            }

            return initializers;
        } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new PippoRuntimeException("Failed to locate Initializers", e);
        }
    }

    @Override
    public String toString() {
        String toString = (getApplicationName() + " " + getApplicationVersion()).trim();

        return toString.isEmpty() ? super.toString() : toString;
    }

}
