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
import ro.pippo.core.session.SessionHttpServletRequest;
import ro.pippo.core.session.SessionManager;
import ro.pippo.core.util.PippoUtils;
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

    private static final String slash = "/";

    private RouteDispatcher routeDispatcher;
    private Application application;
    private String filterPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info(PippoUtils.getPippoLogo());

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

            log.debug("Initializing Route Dispatcher");
            routeDispatcher = new RouteDispatcher(application);
            routeDispatcher.init();

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
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

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

        // create response
        Response response = new Response(httpServletResponse, application);

        // check for (custom) session manager
        Request request;
        SessionManager sessionManager = application.getSessionManager();
        if (sessionManager != null) {
            final SessionHttpServletRequest sessionHttpServletRequest = new SessionHttpServletRequest(httpServletRequest, sessionManager);
            response.getFinalizeListeners().add(new ResponseFinalizeListener() {

                @Override
                public void onFinalize(Response response) {
                    sessionHttpServletRequest.commitSession(httpServletResponse);
                }

            });

            request = new Request(sessionHttpServletRequest, application);
        } else {
            request = new Request(httpServletRequest, application);
        }

        // dispatch route(s)
        routeDispatcher.dispatch(request, response);
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

}
