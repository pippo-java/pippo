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
import ro.pippo.core.route.DefaultRouteContextFactory;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteContextFactory;
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
import java.util.List;
import java.util.Properties;

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
    private Application application;
    private List<Initializer> initializers;
    private String filterPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info(PIPPO_LOGO, readPippoVersion());

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

        try {
            String contextPath = StringUtils.addStart(filterConfig.getServletContext().getContextPath(), "/");
            application.setContextPath(contextPath);
            log.debug("Serving application on context path '{}'", contextPath);

            initializers = new ArrayList<>();

            routeContextFactory = getRouteContextFactory();
            initializers.add(routeContextFactory);
            log.debug("RouteContext factory is '{}'", routeContextFactory.getClass().getName());

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
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        // TODO test for redirect
        // no redirect; process the request

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
        final Request request = new Request(httpServletRequest, application);
        final Response response = new Response(httpServletResponse, application);

        ErrorHandler errorHandler = application.getErrorHandler();

        RouteContext routeContext = null;
        try {
            Router router = application.getRouter();
            List<RouteMatch> routeMatches = router.findRoutes(relativePath, requestMethod);
            routeContext = routeContextFactory.createRouteContext(application, request, response, routeMatches);

            if (routeMatches.isEmpty()) {
                errorHandler.handle(HttpConstants.StatusCode.NOT_FOUND, routeContext);
            } else {
                // Force the initial Response status code to NOT_FOUND.
                // The chain is expected to properly set a Response status code.
                response.notFound();

                processFlash(routeContext);
            }

            routeContext.next();

            if (!response.isCommitted()) {
                log.debug("Auto-committing response for {} '{}'", requestMethod, relativePath);
                if (response.getStatus() >= HttpConstants.StatusCode.BAD_REQUEST) {
                    // delegate response to the error handler.
                    // this will generate response content appropriate for the request/
                    errorHandler.handle(response.getStatus(), routeContext);
                } else {
                    response.commit();
                }
            }
        } catch (Exception e) {
            errorHandler.handle(e, routeContext);
        } finally {
            routeContext.runFinallyRoutes();
            log.debug("Returned status code {} for {} '{}'", response.getStatus(), requestMethod, relativePath);
        }
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
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
            try {
                for (Initializer initializer : initializers) {
                    initializer.destroy(application);
                }
                application.destroy();

                log.info("Pippo destroyed");
            } finally {
                application = null;
            }
        }
    }

    private boolean shouldIgnorePath(String requestUri) {
        for (String path : application.getRouter().getIgnorePaths()) {
            if (requestUri.startsWith(path)) {
                return true;
            }
        }

        return false;
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
    private String readPippoVersion() {
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

        if (routeContext.hasSession()) {
            // get flash from session
            Session session = routeContext.getSession();
            flash = session.remove("flash");
            // put an empty flash (outcoming flash) in session; defense against session.get("flash")
            session.put("flash", new Flash());
        }

        if (flash == null) {
            flash = new Flash();
        }

        // make current flash available to templates
        routeContext.setLocal("flash", flash);
    }

}
