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
package ro.pippo.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.handlers.DefaultServlet;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.AbstractWebServer;
import ro.pippo.core.Application;
import ro.pippo.core.PippoFilter;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoServletContextListener;
import ro.pippo.core.WebServer;
import ro.pippo.undertow.websocket.UndertowWebSocketFilter;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * An implementation of WebServer based on Undertow.
 *
 * @see <a href="http://undertow.io">Undertow</a>
 *
 * @author James Moger
 */
@MetaInfServices(WebServer.class)
public class UndertowServer extends AbstractWebServer<UndertowSettings> {

    private static final Logger log = LoggerFactory.getLogger(UndertowServer.class);

    private Undertow server;
    private DeploymentManager pippoDeploymentManager;

    @Override
    public void start() {
        try {
            pippoDeploymentManager = createPippoDeploymentManager();
            HttpHandler pippoHandler = pippoDeploymentManager.start();

            HttpHandler contextHandler = createContextHandler(pippoHandler);
            GracefulShutdownHandler rootHandler = new GracefulShutdownHandler(contextHandler);
            server = createServer(rootHandler);

            String version = server.getClass().getPackage().getImplementationVersion();
            log.info("Starting Undertow Server {} on port {}", version, getSettings().getPort());

            server.start();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            try {
                String version = server.getClass().getPackage().getImplementationVersion();
                log.info("Stopping Undertow {} on port {}", version, getSettings().getPort());

                server.stop();

                pippoDeploymentManager.undeploy();
            } catch (Exception e) {
                throw new PippoRuntimeException(e, "Cannot stop Undertow Server");
            }
        }
    }

    @Override
    protected UndertowSettings createDefaultSettings() {
        return new UndertowSettings(getApplication().getPippoSettings());
    }

    protected Undertow createServer(HttpHandler contextHandler) {
        Builder builder = Undertow.builder();
        // TODO is it a better option?
        if (getSettings().getBufferSize() > 0) {
            builder.setBufferSize(getSettings().getBufferSize());
        }
        // method builder.setBuffersPerRegion is deprecated
        /*
        if (getSettings().getBuffersPerRegion() > 0) {
            builder.setBuffersPerRegion(getSettings().getBuffersPerRegion());
        }
        */
        if (getSettings().getDirectBuffers()) {
            builder.setDirectBuffers(getSettings().getDirectBuffers());
        }
        if (getSettings().getIoThreads() > 0) {
            builder.setIoThreads(getSettings().getIoThreads());
        }
        if (getSettings().getWorkerThreads() > 0) {
            builder.setWorkerThreads(getSettings().getWorkerThreads());
        }

        if (getSettings().getKeystoreFile() == null) {
            // HTTP
            builder.addHttpListener(getSettings().getPort(), getSettings().getHost());
        } else {
            // HTTPS
            builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
            try {
                KeyStore keyStore = loadKeyStore(getSettings().getKeystoreFile(), getSettings().getKeystorePassword());
                KeyStore trustStore = loadKeyStore(getSettings().getTruststoreFile(), getSettings().getTruststorePassword());
                SSLContext sslContext = createSSLContext(keyStore, trustStore);
                builder.addHttpsListener(getSettings().getPort(), getSettings().getHost(), sslContext);
            } catch (Exception e) {
                throw new PippoRuntimeException(e, "Failed to setup an Undertow SSL listener!");
            }
        }

        // add undertow options
        getSettings().addUndertowOptions(builder);
        builder.setHandler(contextHandler);

        return builder.build();
    }

    protected HttpHandler createContextHandler(HttpHandler pippoHandler) throws ServletException {
        String contextPath = getSettings().getContextPath();

        // create a handler than redirects non-contact requests to the context
        PathHandler contextHandler = Handlers.path(Handlers.redirect(contextPath));

        // add the handler with the context prefix
        contextHandler.addPrefixPath(contextPath, pippoHandler);

        return contextHandler;
    }

    @Override
    protected PippoFilter createPippoFilter() {
        return new UndertowWebSocketFilter();
    }

    protected DeploymentManager createPippoDeploymentManager() throws ServletException {
        DeploymentInfo info = Servlets.deployment();
        info.setDeploymentName("Pippo");
        info.setClassLoader(this.getClass().getClassLoader());
        info.setContextPath(getSettings().getContextPath());
        info.setIgnoreFlush(true);

        // inject application as context attribute
        info.addServletContextAttribute(PIPPO_APPLICATION, getApplication());

        // add pippo filter
        addPippoFilter(info);

        // add initializers
        info.addListener(new ListenerInfo(PippoServletContextListener.class));

        // add listeners
        listeners.forEach(listener -> info.addListener(new ListenerInfo(listener)));

        ServletInfo defaultServlet = new ServletInfo("DefaultServlet", DefaultServlet.class);
        defaultServlet.addMapping("/");

        MultipartConfigElement multipartConfig = createMultipartConfigElement();
        defaultServlet.setMultipartConfig(multipartConfig);
        info.addServlets(defaultServlet);

        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(info);
        deploymentManager.deploy();

        return deploymentManager;
    }

    private MultipartConfigElement createMultipartConfigElement() {
        Application application = getApplication();
        String location = application.getUploadLocation();
        long maxFileSize = application.getMaximumUploadSize();

        return new MultipartConfigElement(location, maxFileSize, -1L, 0);
    }

    private void addPippoFilter(DeploymentInfo info) {
        if (pippoFilterPath == null) {
            pippoFilterPath = "/*"; // default value
        }

        info.addFilter(new FilterInfo("PippoFilter", PippoFilter.class, new ImmediateInstanceFactory<>(getPippoFilter())));
        info.addFilterUrlMapping("PippoFilter", pippoFilterPath, DispatcherType.REQUEST);
        log.debug("Using pippo filter for path '{}'", pippoFilterPath);
    }

    private SSLContext createSSLContext(final KeyStore keyStore, final KeyStore trustStore) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, getSettings().getKeystorePassword().toCharArray());
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        SSLContext sslContext;
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);

        return sslContext;
    }

    private KeyStore loadKeyStore(String filename, String password) throws Exception {
        KeyStore loadedKeystore = KeyStore.getInstance("JKS");
        File file = new File(filename);
        if (file.exists()) {
            try (InputStream stream = new FileInputStream(file)) {
                loadedKeystore.load(stream, password.toCharArray());
            }
        } else {
            log.error("Failed to find keystore '{}'!", filename);
        }

        return loadedKeystore;
    }

}
