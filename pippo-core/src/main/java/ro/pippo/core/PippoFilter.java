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
import ro.pippo.core.route.DefaultRouteHandlerChainFactory;
import ro.pippo.core.route.RouteHandlerChain;
import ro.pippo.core.route.RouteHandlerChainFactory;
import ro.pippo.core.route.RouteMatch;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.ClasspathUtils;
import ro.pippo.core.util.ServiceLocator;
import ro.pippo.core.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public class PippoFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(PippoFilter.class);

    /**
     * The name of the root path parameter that specifies the root dir of the app.
     */
    public static final String FILTER_MAPPING_PARAM = "filterMappingUrlPattern";

    /**
     * The name of the context parameter that specifies application class.
     */
    public static final String APPLICATION_CLASS_PARAM = "applicationClassName";

    /**
     * Name of parameter used to express a comma separated list of paths that should be ignored.
     */
    public static final String IGNORE_PATHS_PARAM = "ignorePaths";

    /**
     * The name of the context parameter that specifies the runtime mode.
     */
    public static final String MODE_PARAM = "mode";

    private static final String slash = "/";

    private final String PIPPO_LOGO = "\n"
        + " ____  ____  ____  ____  _____\n"
        + "(  _ \\(_  _)(  _ \\(  _ \\(  _  )\n"
        + " ) __/ _)(_  ) __/ ) __/ )(_)(   http://pippo.ro\n"
        + "(__)  (____)(__)  (__)  (_____)  {}\n";

    private RouteContextFactory routeContextFactory;
    private RouteHandlerChainFactory routeHandlerChainFactory;
    private Application application;
    private List<Initializer> initializers;
    private Set<String> ignorePaths;
    private String filterPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info(PIPPO_LOGO, readPippoVersion());

        if (ignorePaths == null) {
            initIgnorePaths(filterConfig);
        }
        log.debug("Ignore paths '{}'", ignorePaths);

        if (filterPath == null) {
            initFilterPath(filterConfig);
        }
        log.debug("The filter path is '{}'", filterPath);

        // check for runtime mode in filter init parameter
        String mode = filterConfig.getInitParameter(MODE_PARAM);
        if (!StringUtils.isNullOrEmpty(mode)) {
            System.setProperty(PippoConstants.SYSTEM_PROPERTY_PIPPO_MODE, mode);
        }

        if (application == null) {
            createApplication(filterConfig);
            log.debug("Created application '{}'", application);
        }

        ThreadContext.setApplication(application);

        try {
            String contextPath = StringUtils.addStart(filterConfig.getServletContext().getContextPath(), "/");
            application.setContextPath(contextPath);
            log.debug("Serving application on context path '{}'", contextPath);

            initializers = new ArrayList<>();

            routeContextFactory = getRouteContextFactory();
            initializers.add(routeContextFactory);
            log.debug("RouteContext factory is '{}'", routeContextFactory.getClass().getName());

            routeHandlerChainFactory = getRouteHandlerChainFactory();
            initializers.add(routeHandlerChainFactory);
            log.debug("Route handler chain factory is '{}'", routeHandlerChainFactory.getClass().getName());

            initializers.addAll(getInitializers());
            for (Initializer initializer : initializers) {
                initializer.init(application);
            }

            log.debug("Initializing application '{}'", application);
            application.init();

            String runtimeMode = application.getRuntimeMode().toString().toUpperCase();
            log.info("Pippo started ({})", runtimeMode);
        } catch (Exception e) {
            destroy();
            throw new ServletException(e);
        } finally {
            ThreadContext.detach();
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws IOException, ServletException {
        final ThreadContext previousThreadContext = ThreadContext.detach();

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        // TODO test for redirect
        // no redirect; process the request
        ThreadContext.setApplication(application);

        String requestMethod = httpServletRequest.getMethod();

        // create a URI to automatically decode the path
        URI uri = URI.create(httpServletRequest.getRequestURL().toString());
        String requestUri = uri.getPath();
        String relativePath = getRelativePath(httpServletRequest.getContextPath(), requestUri);
        log.trace("The relative path for '{}' is '{}'", requestUri, relativePath);

        // check for ignore path
        if (shouldIgnorePath(relativePath)) {
            log.debug("Ignoring request '{}'", relativePath);
            if (chain != null) {
                chain.doFilter(servletRequest, servletResponse);
            }

            return;
        }

        log.debug("Request {} '{}'", requestMethod, relativePath);
        final RouteContext routeContext = routeContextFactory.createRouteContext(application, httpServletRequest, httpServletResponse);
        ErrorHandler errorHandler = application.getErrorHandler();

        processFlash(routeContext);

        RouteHandlerChain handlerChain = null;
        try {
            // Force the initial Response status code to Integer.MAX_VALUE.
            // The chain is expected to properly set a Response status code.
            // Note: Some containers (e.g. Jetty) prohibit setting 0.
            routeContext.getResponse().status(Integer.MAX_VALUE);

            Router router = application.getRouter();
            List<RouteMatch> routeMatches = router.findRoutes(relativePath, requestMethod);

            if (routeMatches.isEmpty()) {
                errorHandler.handle(HttpConstants.StatusCode.NOT_FOUND, routeContext);
                handlerChain = routeHandlerChainFactory.createChain(routeContext, new ArrayList<RouteMatch>());
            } else {
                handlerChain = routeHandlerChainFactory.createChain(routeContext, routeMatches);
            }

            handlerChain.next();

            if (!routeContext.getResponse().isCommitted()) {
                if (routeContext.getResponse().getStatus() == Integer.MAX_VALUE) {
                    log.info("Handlers in chain did not set a status code for {} '{}'", requestMethod, relativePath);
                    routeContext.getResponse().notFound();
                }
                log.debug("Auto-committing response for {} '{}'", requestMethod, relativePath);
                if (routeContext.getResponse().getStatus() >= HttpConstants.StatusCode.BAD_REQUEST) {
                    // delegate response to the error handler.
                    // this will generate response content appropriate for the request/
                    errorHandler.handle(routeContext.getResponse().getStatus(), routeContext);
                } else {
                    routeContext.getResponse().commit();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            errorHandler.handle(e, routeContext);
        } finally {
            handlerChain.runFinallyRoutes();
            log.debug("Returned status code {} for {} '{}'", routeContext.getResponse().getStatus(), requestMethod, relativePath);
            ThreadContext.restore(previousThreadContext);
        }
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Set<String> getIgnorePaths() {
        return ignorePaths;
    }

    public void setIgnorePaths(Set<String> ignorePaths) {
        this.ignorePaths = ignorePaths;
    }

    public String getFilterPath() {
        return filterPath;
    }

    public void setFilterPath(String filterPath) {
        this.filterPath = filterPath;
    }

    @Override
    public void destroy() {
        if (application != null) {
            ThreadContext.setApplication(application);

            try {
                for (Initializer initializer : initializers) {
                    initializer.destroy(application);
                }
                application.destroy();

                log.info("Pippo destroyed");
            } finally {
                ThreadContext.detach();
                application = null;
            }
        }
    }

    private boolean shouldIgnorePath(String path) {
        if (ignorePaths.size() > 0) {
            if (path != null && !path.isEmpty()) {
                for (String ignorePath : ignorePaths) {
                    if (path.startsWith(ignorePath)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void initIgnorePaths(FilterConfig filterConfig) {
        ignorePaths = new HashSet<>();
        String paths = filterConfig.getInitParameter(IGNORE_PATHS_PARAM);
        if (paths != null && !paths.isEmpty()) {
            String[] parts = paths.split(",");
            for (String path : parts) {
                path = path.trim();
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                ignorePaths.add(path);
            }
        }
    }

    private void initFilterPath(FilterConfig filterConfig) {
        initFilterPathFromConfig(filterConfig);
        if (filterPath == null) {
            initFilterPathFromWebXml(filterConfig);
        }

        if (filterPath == null) {
            StringBuilder message = new StringBuilder();
            message.append("Unable to determine filter path from filter init-param, web.xml.");
            message.append("Assuming user will set filter path manually by calling setFilterPath(String)");
            log.warn(message.toString());
        }
    }

    private void initFilterPathFromConfig(FilterConfig filterConfig) {
        String filterMapping = filterConfig.getInitParameter(FILTER_MAPPING_PARAM);
        if (filterMapping != null) {
            if (filterMapping.equals("/*")) {
                filterMapping = "";
            } else if (!filterMapping.startsWith("/") || !filterMapping.endsWith("/*")) {
                throw new PippoRuntimeException("Your {} must start with \"/\" and end with \"/*\". It is: ",
                    FILTER_MAPPING_PARAM, filterMapping);
            } else {
                // remove leading "/" and trailing "*"
                filterMapping = filterMapping.substring(1, filterMapping.length() - 1);
            }
        }

        filterPath = filterMapping;
    }

    private void initFilterPathFromWebXml(FilterConfig filterConfig) {
        String filterName = filterConfig.getFilterName();
        FilterRegistration filterRegistration = filterConfig.getServletContext().getFilterRegistration(filterName);
        Collection<String> mappings = filterRegistration.getUrlPatternMappings();
        int size = mappings.size();

        if (size > 1) {
            throw new PippoRuntimeException("Expected one filter path for '{}' but found multiple", filterName);
        }

        if (size == 1) {
            String urlPattern = mappings.iterator().next();
            filterPath = urlPattern.substring(1, urlPattern.length() - 1);
        }
    }

    private void createApplication(FilterConfig filterConfig) throws ServletException {
        String applicationClassName = filterConfig.getInitParameter(APPLICATION_CLASS_PARAM);
        if (applicationClassName == null) {
            log.error("Filter init param '{}' is missing", APPLICATION_CLASS_PARAM);
            throw new ServletException("Cannot found application class name");
        }
        try {
            Class<?> applicationClass = Class.forName(applicationClassName);
            application = (Application) applicationClass.newInstance();
        } catch (Exception e) {
            log.error("Cannot create application with className '{}'", applicationClassName, e);
            throw new ServletException(e);
        }
    }

    private String getRelativePath(String contextPath, String path) {
        path = path.substring(contextPath.length());
        if (path.length() > 0) {
            path = path.substring(1);
        }
        if (!path.startsWith(filterPath) && filterPath.equals(path + slash)) {
            path += slash;
        }
        if (path.startsWith(filterPath)) {
            path = path.substring(filterPath.length());
        }
        if (!path.startsWith(slash)) {
            path = slash + path;
        }
        if (path.length() > 1 && path.endsWith(slash)) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    private RouteContextFactory getRouteContextFactory() {
        RouteContextFactory factory = ServiceLocator.locate(RouteContextFactory.class);
        if (factory == null) {
            factory = new DefaultRouteContextFactory();
        }

        return factory;
    }

    private RouteHandlerChainFactory getRouteHandlerChainFactory() {
        RouteHandlerChainFactory factory = ServiceLocator.locate(RouteHandlerChainFactory.class);
        if (factory == null) {
            factory = new DefaultRouteHandlerChainFactory();
        }

        return factory;
    }

    private List<Initializer> getInitializers() throws Exception {
        List<Initializer> initializers = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();
        Enumeration<URL> urls = classLoader.getResources("pippo.properties");
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            log.debug("Read '{}'", url.getFile());
            Reader reader = new InputStreamReader(url.openStream(), "UTF-8");
            Properties properties = new Properties();
            properties.load(reader);
            String initializerClassName = properties.getProperty("initializer");
            log.debug("Found initializer '{}'", initializerClassName);
            Class<Initializer> initializerClass = (Class<Initializer>) classLoader.loadClass(initializerClassName);
            initializers.add(initializerClass.newInstance());
        }

        return initializers;
    }

    /**
     * Simply reads a property resource file that contains the version of this
     * Pippo build. Helps to identify the Pippo version currently running.
     *
     * @return The version of Pippo. Eg. "1.6-SNAPSHOT" while developing of "1.6" when released.
     */
    private final String readPippoVersion() {
        // and the key inside the properties file.
        String PIPPO_VERSION_PROPERTY_KEY = "pippo.version";

        String pippoVersion;

        try {
            Properties prop = new Properties();
            URL url = ClasspathUtils.locateOnClasspath(PippoConstants.LOCATION_OF_PIPPO_BUILTIN_PROPERTIES);
            InputStream stream = url.openStream();
            prop.load(stream);

            pippoVersion = prop.getProperty(PIPPO_VERSION_PROPERTY_KEY);
        } catch (Exception e) {
            //this should not happen. Never.
            throw new PippoRuntimeException("Something is wrong with your build. Cannot find resource {}",
                PippoConstants.LOCATION_OF_PIPPO_BUILTIN_PROPERTIES);
        }

        return pippoVersion;
    }

    private void processFlash(RouteContext routeContext) {
        Flash flash = null;

        Session session = routeContext.getRequest().getSession(false);
        if (session != null) {
            // get flash from session
            flash = session.remove("flash");
            // put an empty flash (outcoming flash) in session; defense against session.get("flash")
            session.put("flash", new Flash());
        }

        if (flash == null) {
            flash = new Flash();
        }

        // make current flash available to templates
        routeContext.getResponse().bind("flash", flash);
    }

}
