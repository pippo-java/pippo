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
package ro.pippo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.RouteDispatcher;
import ro.pippo.core.util.PippoUtils;
import ro.pippo.core.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

/**
 * @author James Moger
 */
public class PippoServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PippoServlet.class);

    /**
     * The name of the context parameter that specifies application class.
     */
    public static final String APPLICATION_CLASS_PARAM = "applicationClassName";

    /**
     * The name of the context parameter that specifies the runtime mode.
     */
    public static final String MODE_PARAM = "mode";

    private Application application;

    private RouteDispatcher routeDispatcher;

    public PippoServlet() {
        super();
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Application getApplication() {
        return application;
    }

    @Override
    public void init(ServletConfig servletConfig) {
        if (System.getProperty("pippo.hideLogo") == null) {
            log.info(PippoUtils.getPippoLogo());
        }

        // check for runtime mode in filter init parameter
        String mode = servletConfig.getInitParameter(MODE_PARAM);
        if (!StringUtils.isNullOrEmpty(mode)) {
            System.setProperty(PippoConstants.SYSTEM_PROPERTY_PIPPO_MODE, mode);
        }

        if (application == null) {
            createApplication(servletConfig);
            log.debug("Created application '{}'", application);
        }

        ServletContext servletContext = servletConfig.getServletContext();

        // save the servlet context object in application
        application.setServletContext(servletContext);

        // set the application as an attribute of the servlet container
        if (servletContext.getAttribute(WebServer.PIPPO_APPLICATION) == null) {
            servletContext.setAttribute(WebServer.PIPPO_APPLICATION, application);
        }

        String contextPath = StringUtils.addStart(servletContext.getContextPath(), "/");
        application.getRouter().setContextPath(contextPath);
        log.debug("Serving application on context path '{}'", contextPath);

        log.debug("Initializing Route Dispatcher");
        routeDispatcher = new RouteDispatcher(application);
        routeDispatcher.init();

        String runtimeMode = application.getRuntimeMode().toString().toUpperCase();
        log.info("Pippo started ({})", runtimeMode);
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        // set the application path from the servlet request since we don't know it at initialization
        String applicationPath = application.getRouter().getContextPath() + httpServletRequest.getServletPath();
        application.getRouter().setApplicationPath(applicationPath);

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

        log.debug("Request {} '{}'", request.getMethod(), requestPath);

        // dispatch route(s)
        routeDispatcher.dispatch(request, response);
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

    private void createApplication(ServletConfig servletConfig) {
        String applicationClassName = servletConfig.getInitParameter(APPLICATION_CLASS_PARAM);
        if (applicationClassName == null) {
            log.error("Servlet init param '{}' is missing", APPLICATION_CLASS_PARAM);
            throw new PippoRuntimeException("Cannot found application class name");
        }

        try {
            Class<?> applicationClass = Class.forName(applicationClassName);
            application = (Application) applicationClass.newInstance();
        } catch (Exception e) {
            log.error("Cannot create application with className '{}'", applicationClassName, e);
            throw new PippoRuntimeException(e);
        }
    }

}
