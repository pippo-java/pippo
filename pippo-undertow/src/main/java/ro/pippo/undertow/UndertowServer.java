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
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.handlers.DefaultServlet;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.AbstractWebServer;
import ro.pippo.core.Application;
import ro.pippo.core.PippoFilter;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.StringUtils;

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
 * @author James Moger
 * @see http://undertow.io
 */
public class UndertowServer extends AbstractWebServer {

    private static final Logger log = LoggerFactory.getLogger(UndertowServer.class);

    Undertow server;
    DeploymentManager pippoDeploymentManager;

    @Override
    public void start() {
        try {
            pippoDeploymentManager = createPippoDeploymentManager();
            HttpHandler pippoHandler = pippoDeploymentManager.start();

            HttpHandler contextHandler = createContextHandler(pippoHandler);
            GracefulShutdownHandler rootHandler = new GracefulShutdownHandler(contextHandler);
            server = createServer(rootHandler);

            String version = server.getClass().getPackage().getImplementationVersion();
            log.info("Starting Undertow Server {} on port {}", version, settings.getPort());

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
                log.info("Stopping Undertow {} on port {}", version, settings.getPort());

                server.stop();

                pippoDeploymentManager.undeploy();
            } catch (Exception e) {
                throw new PippoRuntimeException("Cannot stop Undertow Server", e);
            }
        }
    }

    protected Undertow createServer(HttpHandler contextHandler) {
        Builder builder = Undertow.builder();
        if (settings.getKeystoreFile() == null) {
            // HTTP
            builder.addHttpListener(settings.getPort(), settings.getHost());
        } else {
            // HTTPS
            builder.setServerOption(UndertowOptions.ENABLE_SPDY, true);
            try {
                KeyStore keyStore = loadKeyStore(settings.getKeystoreFile(), settings.getKeystorePassword());
                KeyStore trustStore = loadKeyStore(settings.getTruststoreFile(), settings.getTruststorePassword());
                SSLContext sslContext = createSSLContext(keyStore, trustStore);
                builder.addHttpsListener(settings.getPort(), settings.getHost(), sslContext);
            } catch (Exception e) {
                throw new PippoRuntimeException("Failed to setup an Undertow SSL listener!", e);
            }
        }

        builder.setHandler(contextHandler);

        return builder.build();
    }

    protected HttpHandler createContextHandler(HttpHandler pippoHandler) throws ServletException {
        String contextPath = settings.getContextPath();

        // create a handler than redirects non-contact requests to the context
        PathHandler contextHandler = Handlers.path(Handlers.redirect(contextPath));

        // add the handler with the context prefix
        contextHandler.addPrefixPath(contextPath, pippoHandler);

        return contextHandler;
    }

    protected DeploymentManager createPippoDeploymentManager() throws ServletException {
        DeploymentInfo info = Servlets.deployment();
        info.setDeploymentName("Pippo");
        info.setClassLoader(this.getClass().getClassLoader());
        info.setContextPath(settings.getContextPath());
        info.setIgnoreFlush(true);

        if (StringUtils.isNullOrEmpty(pippoFilterPath)) {
            pippoFilterPath = "/*"; // default value
        }

        info.addFilters(new FilterInfo("PippoFilter", PippoFilter.class, new ImmediateInstanceFactory<>(pippoFilter)));
        info.addFilterUrlMapping("PippoFilter", pippoFilterPath, DispatcherType.REQUEST);

        ServletInfo defaultServlet = new ServletInfo("DefaultServlet", DefaultServlet.class);
        defaultServlet.addMapping("/");

        Application application = pippoFilter.getApplication();
        String location = application.getUploadLocation();
        long maxFileSize = application.getMaximumUploadSize();
        MultipartConfigElement multipartConfig = new MultipartConfigElement(location, maxFileSize, -1L, 0);
        defaultServlet.setMultipartConfig(multipartConfig);
        info.addServlets(defaultServlet);

        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(info);
        deploymentManager.deploy();
        log.debug("Using pippo filter for path '{}'", pippoFilterPath);

        return deploymentManager;
    }

    private SSLContext createSSLContext(final KeyStore keyStore, final KeyStore trustStore) throws Exception {
        KeyManager[] keyManagers;
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, settings.getKeystorePassword().toCharArray());
        keyManagers = keyManagerFactory.getKeyManagers();

        TrustManager[] trustManagers = null;
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory
            .getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        trustManagers = trustManagerFactory.getTrustManagers();

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
