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
import ro.pippo.core.route.RouteDispatcher;
import ro.pippo.core.util.PippoUtils;
import ro.pippo.core.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

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

    private RouteDispatcher routeDispatcher;
    private Application application;
    private String filterPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (System.getProperty("pippo.hideLogo") == null) {
            log.info(PippoUtils.getPippoLogo());
        }

        // check for runtime mode in filter init parameter
        String mode = filterConfig.getInitParameter(MODE_PARAM);
        if (!StringUtils.isNullOrEmpty(mode)) {
            System.setProperty(PippoConstants.SYSTEM_PROPERTY_PIPPO_MODE, mode);
        }

        if (application == null) {
            createApplication(filterConfig);
            log.debug("Created application '{}'", application);
        }

        ServletContext servletContext = filterConfig.getServletContext();

        // save the servlet context object in application
        application.setServletContext(servletContext);

        // set the application as an attribute of the servlet container
        if (servletContext.getAttribute(WebServer.PIPPO_APPLICATION) == null) {
            servletContext.setAttribute(WebServer.PIPPO_APPLICATION, application);
        }

        try {
            String contextPath = StringUtils.addStart(servletContext.getContextPath(), "/");
            application.getRouter().setContextPath(contextPath);

            if (filterPath == null) {
                initFilterPath(filterConfig);
            }
            String applicationPath = StringUtils.addEnd(contextPath, "/") + StringUtils.removeStart(filterPath, "/");
            application.getRouter().setApplicationPath(applicationPath);

            if (!contextPath.equals(applicationPath)) {
                log.debug("Context path is '{}'", contextPath);
            }
            log.debug("Serving application on path '{}'", applicationPath);

            log.debug("Initializing Route Dispatcher");
            routeDispatcher = new RouteDispatcher(application);
            routeDispatcher.init();

            String runtimeMode = application.getRuntimeMode().toString().toUpperCase();
            log.info("Pippo started ({})", runtimeMode);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

        // create Request, Response objects
        RequestResponseFactory requestResponseFactory = application.getRequestResponseFactory();
        RequestResponse requestResponse = requestResponseFactory.createRequestResponse(httpServletRequest, httpServletResponse);
        Request request = requestResponse.getRequest();
        Response response = requestResponse.getResponse();

        // create a URI to automatically decode the path
        URI uri = URI.create(httpServletRequest.getRequestURL().toString());
        String requestUri = uri.getPath();
        String requestPath = request.getPath();

        log.trace("The relative path for '{}' is '{}'", requestUri, requestPath);

        // check for ignore path
        if (shouldIgnorePath(requestPath)) {
            log.debug("Ignoring request '{}'", requestPath);
            if (chain != null) {
                chain.doFilter(servletRequest, servletResponse);
            }

            return;
        }

        log.debug("Request {} '{}'", request.getMethod(), requestPath);

        processRequest(request, response);
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public void destroy() {
        if (application != null) {
            try {
                application.destroy();

                log.info("Pippo destroyed");
            } finally {
                application = null;
            }
        }
    }

    protected void processRequest(Request request, Response response) throws IOException, ServletException {
        // dispatch route(s)
        routeDispatcher.dispatch(request, response);
    }

    /**
     * Sets the filter path instead of reading it from {@code web.xml}.
     *
     * Please note that you must subclass {@code PippoFilter.init(FilterConfig)} and set your filter path
     * before you call {@code super.init(filterConfig)}.
     * For example:
     * {@code
     * class MyPippoFilter extends PippoFilter {
     *
     *     @Override
     *     public void init(FilterConfig filterConfig) throws ServletException {
     *         setFilterPath("/*");
     *         super.init(filterConfig);
     *     }
     *
     * }
     * }
     *
     * @param urlPattern
     */
    protected void setFilterPath(String urlPattern) {
        initFilterPath(urlPattern);
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
            initFilterPath(filterMapping);
        }
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
            initFilterPath(urlPattern);
        }
    }

    private void initFilterPath(String urlPattern) {
        validateFilterUrlPattern(urlPattern);
        // remove leading "/" and trailing "*"
        filterPath = urlPattern.substring(1, urlPattern.length() - 1);
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

    static void validateFilterUrlPattern(String urlPattern) {
        if (!urlPattern.startsWith("/") || !urlPattern.endsWith("/*")) {
            throw new PippoRuntimeException("Your '{}' must start with '{}' and end with '{}'. It's '{}'",
                FILTER_MAPPING_PARAM, "/", "/*", urlPattern);
        }
    }

}
