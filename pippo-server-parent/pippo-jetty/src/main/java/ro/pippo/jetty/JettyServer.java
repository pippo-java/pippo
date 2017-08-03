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
package ro.pippo.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.AbstractWebServer;
import ro.pippo.core.Application;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoFilter;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoServletContextListener;
import ro.pippo.core.WebServer;

import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

/**
 * Must have a zero-argument constructor so that they can be instantiated during loading.
 *
 * @author Decebal Suiu
 */
@MetaInfServices(WebServer.class)
public class JettyServer extends AbstractWebServer<JettySettings> {

    private static final Logger log = LoggerFactory.getLogger(JettyServer.class);

    private Server server;

    @Override
    public void start() {
        server = createServer();

        ServerConnector serverConnector = createServerConnector(server);
        serverConnector.setIdleTimeout(TimeUnit.HOURS.toMillis(1));
        serverConnector.setSoLingerTime(-1);
        serverConnector.setHost(getSettings().getHost());
        serverConnector.setPort(getSettings().getPort());

        ServerConnector[] connectors = new ServerConnector[1];
        connectors[0] = serverConnector;
        server.setConnectors(connectors);

        Handler pippoHandler = createPippoHandler();
        server.setHandler(pippoHandler);

        String version = server.getClass().getPackage().getImplementationVersion();
        log.info("Starting Jetty Server {} on port {}", version, getSettings().getPort());

        try {
            server.start();
        } catch (Exception e) {
            log.error("Unable to launch Jetty", e);
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); //don't lose interrupted state: https://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                throw new PippoRuntimeException(e, "Interrupted while waiting for Jetty Server to stop.");
            } catch (Exception e) {
                throw new PippoRuntimeException(e, "Cannot stop Jetty Server");
            }
        }
    }

    @Override
    protected JettySettings createDefaultSettings() {
        return new JettySettings(getApplication().getPippoSettings());
    }

    protected Server createServer() {
        if (getSettings().getMaxThreads() > 0) {
            int maxThreads = getSettings().getMaxThreads();
            int minThreads = getSettings().getMinThreads();
            if (minThreads == 0) {
                minThreads = JettySettings.DEFAULT_MIN_THREADS;
            }
            int idleTimeout = getSettings().getIdleTimeout();
            if (idleTimeout == 0) {
                idleTimeout = JettySettings.DEFAULT_IDLE_TIMEOUT;
            }

            return new Server(new QueuedThreadPool(maxThreads, minThreads, idleTimeout));
        }

        return new Server();
    }

    /**
     * Jetty treats non-URL paths are file paths interpreted in the current working directory.
     * Provide ability to accept paths to resources on the Classpath.
     * @param path
     * @param name Descriptive name of what  is for. Used in logs, error messages
     * @return Path in a format Jetty will understand, even if it is a Classpath-relative path.
     */
    private String asJettyFriendlyPath(String path, String name) {
        try {
            new URL(path);
            log.debug("Defer interpretation of {} URL '{}' to Jetty", name, path);
            return path;
        } catch (MalformedURLException e) {
            //Expected. We've got a path and not a URL
            Path p = Paths.get(path);
            if (Files.exists(Paths.get(path))) {
                //Jetty knows how to find files on the file system
                log.debug("Located {} '{}' on file system", name, path);
                return path;
            } else {
                //Maybe it's a resource on the Classpath. Jetty needs that converted to a URL.
                //(e.g. "jar:file:/path/to/my.jar!<path>")
                URL url = JettyServer.class.getResource(path);
                if (url != null) {
                    log.debug("Located {} '{}' on Classpath", name, path);
                    return url.toExternalForm();
                } else {
                    throw new IllegalArgumentException(String.format("%s '%s' not found", name, path));
                }
            }
        }
    }

    protected ServerConnector createServerConnector(Server server) {
        String keyStoreFile = getSettings().getKeystoreFile();
        if (keyStoreFile == null) {
            return new ServerConnector(server);
        }
        SslContextFactory sslContextFactory = new SslContextFactory(asJettyFriendlyPath(keyStoreFile, "Keystore file"));

        if (getSettings().getKeystorePassword() != null) {
            sslContextFactory.setKeyStorePassword(getSettings().getKeystorePassword());
        }
        String truststoreFile = getSettings().getTruststoreFile();
        if (truststoreFile != null) {
            sslContextFactory.setTrustStorePath(asJettyFriendlyPath(truststoreFile, "Truststore file"));
        }
        if (getSettings().getTruststorePassword() != null) {
            sslContextFactory.setTrustStorePassword(getSettings().getTruststorePassword());
        }

        return new ServerConnector(server, sslContextFactory);
    }

    protected ServletContextHandler createPippoHandler() {
        MultipartConfigElement multipartConfig = createMultipartConfigElement();
        ServletContextHandler handler = new PippoHandler(ServletContextHandler.SESSIONS, multipartConfig);
        handler.setContextPath(getSettings().getContextPath());

        // inject application as context attribute
        handler.setAttribute(PIPPO_APPLICATION, getApplication());

        // add pippo filter
        addPippoFilter(handler);

        // add initializers
        handler.addEventListener(new PippoServletContextListener());

        // all listeners
        listeners.forEach(listener -> {
            try {
                handler.addEventListener(listener.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new PippoRuntimeException(e);
            }
        });

        return handler;
    }

    /**
     * Check the existence of {@code org.eclipse.jetty.websocket} as dependency
     * and add {@link ro.pippo.jetty.websocket.JettyWebSocketFilter} instead of {@link PippoFilter}.
     *
     * @return
     */
    @Override
    protected PippoFilter createPippoFilter() {
        try {
            // try to load a class from jetty.websocket
            Class.forName("org.eclipse.jetty.websocket.api.WebSocketListener");
        } catch (ClassNotFoundException e) {
            return super.createPippoFilter();
        }

        try {
            // create an instance of JettyWebSocketFilter
            Class<?> pippoFilterClass = Class.forName("ro.pippo.jetty.websocket.JettyWebSocketFilter");
            return (PippoFilter) pippoFilterClass.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new PippoRuntimeException(e);
        }
    }

    private MultipartConfigElement createMultipartConfigElement() {
        Application application = getApplication();
        String location = application.getUploadLocation();
        long maxFileSize = application.getMaximumUploadSize();

        return new MultipartConfigElement(location, maxFileSize, -1L, 0);
    }

    private void addPippoFilter(ServletContextHandler handler) {
        if (pippoFilterPath == null) {
            pippoFilterPath = "/*"; // default value
        }

        EnumSet<DispatcherType> dispatches = EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR);

        FilterHolder pippoFilterHolder = new FilterHolder(getPippoFilter());
        handler.addFilter(pippoFilterHolder, pippoFilterPath, dispatches);
        log.debug("Using pippo filter for path '{}'", pippoFilterPath);
    }

    /**
     * Inject a MultipartConfig in a filter.
     */
    private static class PippoHandler extends ServletContextHandler {

        private MultipartConfigElement multipartConfig;

        private PippoHandler(int options, MultipartConfigElement multipartConfig) {
            super(options);

            this.multipartConfig = multipartConfig;
        }

        @Override
        public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

            if (isMultipartRequest(request)) {
                baseRequest.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfig);
            }

            super.doHandle(target, baseRequest, request, response);
        }

        private boolean isMultipartRequest(HttpServletRequest request) {
            return HttpConstants.Method.POST.equalsIgnoreCase(request.getMethod())
                && request.getContentType() != null
                && request.getContentType().toLowerCase().startsWith(HttpConstants.ContentType.MULTIPART_FORM_DATA);
        }

    }

}
